/*
Usage:
myhttpd [-f|-t|-p] [<port>]
*/



#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <signal.h>
#include <sys/wait.h>
#include <errno.h>
#include <pthread.h>

const char* usage =
    "   Usage: myhttpd [-f|-t|-p] [<port>]                 \n"
    "                                                      \n"
    "   -f : Create a new process for each reques          \n"
    "   -t : Create a new thread for each request          \n"
    "   -p : Pool of threads                               \n"
    "	1024 < port < 65535							       \n"
    "                                                      \n";

const char* notfound =
    "HTTP/1.0 404 File Not Found\n"
    "Server : CS252 Lab5\n"
    "Content-Type: text/html\n"
    "\n"
    "File Not Found\n";


int QueueLength = 5;

pthread_mutex_t mutex;
pthread_mutexattr_t mattr;



void processRequest(int socket);
void processRequestThread(int socket);
void poolRequest(int socket);
void zombieKiller(int sig);

int main( int argc, char **argv) {
	//kill zombie
	struct sigaction sa_child;
	sa_child.sa_handler = zombieKiller;
	sa_child.sa_flags = SA_RESTART;
	sigemptyset(&sa_child.sa_mask);
	if (sigaction(SIGCHLD, &sa_child, NULL) == -1) {
		perror ("sigchild action");
		exit(1);
	}


	if (argc > 3) {
		fprintf(stderr, "%s", usage);
		exit(-1);
	}

	int flag = 0; //0 for none, 1 for -f, 2 for -t, 3 for -p
	int port = 0;

	if (argc > 1) {
		if (argv[1][0] == '-') { //myhttp -?
			if (argv[1][1] == 'f') { //flag = 1, fork
				flag = 1;
			} else if (argv[1][1] == 't') { //flag = 2, thread
				flag = 2;
			} else if (argv[1][1] == 'p') {	//flag = 3, thread pool
				flag = 3;
			} else {
				fprintf( stderr, "%s", usage);
				exit(-1);
			}

			if (argc == 3) { //myhttp -? port
				port = atoi(argv[2]);
			} else { //myhttp -?, use default port
				port = 2313;
			}
		} else { //myhttp port
			port = atoi(argv[1]);
		}
	} else {  //Only ./myhttpd, default
		flag = 0;
		port = 2313;
	}

	if (port <= 1024 || port >= 65535) {
		fprintf(stderr, "%s", usage);
		exit(-1);
	}

	printf("port = %d\n", port);

	printf("flag = %d\n", flag);




// Set the IP address and port for this server
	struct sockaddr_in serverIPAddress;
	memset( &serverIPAddress, 0, sizeof(serverIPAddress) );
	serverIPAddress.sin_family = AF_INET;
	serverIPAddress.sin_addr.s_addr = INADDR_ANY;
	serverIPAddress.sin_port = htons((u_short) port);

// Allocate a socket
	int masterSocket =  socket(PF_INET, SOCK_STREAM, 0);
	if (masterSocket < 0) {
		perror("socket");
		exit( -1 );
	}

// Set socket options to reuse port. Otherwise we will
// have to wait about 2 minutes before reusing the sae port number
	int optval = 1;
	int err = setsockopt(masterSocket, SOL_SOCKET, SO_REUSEADDR,
	                     (char *) &optval, sizeof(int) );
	if (err) {
		perror("setsockopt");
		exit(-1);
	}
// Bind the socket to the IP address and port
	int error = bind( masterSocket,
	                  (struct sockaddr *)&serverIPAddress,
	                  sizeof(serverIPAddress) );
	if (error) {
		perror("bind");
		exit( -1 );
	}

// Put socket in listening mode and set the
// size of the queue of unprocessed connections
	error = listen( masterSocket, QueueLength);
	if (error) {
		perror("listen");
		exit( -1 );
	}

	struct sockaddr_in clientIPAddress;
	int alen = sizeof(clientIPAddress);

	if (flag == 3) { //for -p

		pthread_t tid[5];
		pthread_attr_t attr;
		pthread_attr_init(&attr);
		pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

		pthread_mutexattr_init(&mattr);
		pthread_mutex_init(&mutex, &mattr);

		for (int i = 0; i < 5; i++) {
			pthread_create(&tid[i], &attr, (void *(*)(void *))poolRequest, (void *)masterSocket);
		}

		pthread_join(tid[0], NULL);

	} else {
		while (1) {
			int slaveSocket = accept(masterSocket, (struct sockaddr *)&clientIPAddress,
			                         (socklen_t*)&alen);

			if (slaveSocket < 0) {
				perror( "accept" );
				exit( -1 );
			}

			if (flag == 1) { //for -f fork process
				if (fork() == 0) {
					processRequest(slaveSocket);
					close(slaveSocket);
					exit(0);
				}
				close(slaveSocket);

			} else if (flag == 2) { //for -t thread based
				//initialize pthread attributes
				//printf("2222\n");
				pthread_t tid;
				pthread_attr_t attr;

				pthread_attr_init(&attr);
				pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
				pthread_create(&tid, &attr, (void * (*)(void*))processRequestThread, (void *)slaveSocket);
				//printf("2222\n");

			} else {
				// Process request.
				//printf("slaveSocket = %d\n", slaveSocket);
				processRequest(slaveSocket);
				// Close socket
				close(slaveSocket);
			}
		}
	}
}

// function to handle killzombie
void zombieKiller(int sig) {
	while (waitpid(-1, NULL, WNOHANG) > 0);
}


void processRequestThread(int socket) {
	printf("thread_id = %d\n", pthread_self());
	printf("socket = %d\n", socket);
	processRequest(socket);
	close(socket);
}

void poolRequest(int socket) {
	while (1) {
		pthread_mutex_lock(&mutex);
		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof(clientIPAddress);
		int slaveSocket = accept(socket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);
		//printf("thread_id = %d\n", pthread_self());

		pthread_mutex_unlock(&mutex);
		//check if accpeted work
		if (slaveSocket < 0) {
			perror("accept");
			exit(-1);
		}

		// Process request
		processRequest(slaveSocket);
		// Close socket
		close(slaveSocket);
	}
}

void processRequest(int socket) {
	int length = 0;
	char currentString[1024]; //the string that get
	char docpath[1024]; //store the path
	char oldchar; //last character that read
	char newchar; //current character
	int gotflag = 0; //flag for GET
	char gotString[1024]; //compare whether the first word is "GET"

	memset(currentString, 0, sizeof(currentString));
	memset(docpath, 0, sizeof(docpath));
	memset(gotString, 0, sizeof(gotString));


	FILE *stream;
	stream = fdopen(socket, "r+");

	int n;
	while (n = read(socket, &newchar, sizeof(newchar))) {
		//printf("newchar = %c\n", newchar);
		if (newchar == ' ') {
			oldchar = newchar;

			if (gotflag == 0) { //"GET"
				gotString[length] = '\0';
				if (strcmp("GET", gotString) == 0) {
					gotflag = 1;
					length = 0;
				} else {
					//Error
					write( socket, "HTTP/1.0 400 Bad Request\n", 25 );
				}
			} else { // docpath
				currentString[length] = '\0';
				strcpy(docpath, currentString);
			}
		} else if (newchar == '\n') {
			break;
		} else { //get the char
			oldchar = newchar;
			//printf("endchar = %c\n", endchar);
			if (gotflag == 0) { //not get the "GET"
				gotString[length] = newchar;
				length++;
				//printf("gotString = %s\n", gotString);
			} else { //"GET"
				currentString[length] = newchar;
				length++;
			}
		}
	}
	printf("docpath = %s\n", docpath);

	char buf[1024];
	memset(buf, 0, 1024);

	while (strcmp(buf, "\r\n")) {
		fgets(buf, 1024, stream);
	}


	//printf("currentString = %s\n", currentString);
	//printf("docpath = %s\n", docpath);

	//map the document path
	char * cwd = (char *)calloc(1024, sizeof(char));
	cwd = getcwd(cwd, 1024);

	if (strncmp(docpath, "/icons", strlen("/icons")) == 0) {
		//if docpath begins with “/icons”
		strcat(cwd, "/http-root-dir/");
		strcat(cwd, docpath);
	} else if (strncmp(docpath, "/htdocs", strlen("/htdocs")) == 0) {
		//if docpath begins with “/htdocs”
		strcat(cwd, "/http-root-dir/");
		strcat(cwd, docpath);
	} else if (strncmp(docpath, "/cgi-bin", strlen("/cgi-bin")) == 0) {
		strcat(cwd, docpath);
		pid_t pid = fork();
		if (pid == 0) {
			// in the child process
			char * exechars[2];
			exechars[0] = cwd;
			exechars[1] = NULL;
			setenv("REQUEST_METHOD", "GET", 1);
			setenv("QUERY_STRING", exechars[0], 1);

			//printf("exechars[0] = %s\n", exechars[0]);
			//printf("exechars[1] = %s\n", exechars[1]);
			write(socket, "HTTP/1.1 200 Document follows\r\n", 31);
			write(socket, "Server: CS 252 Lab5\r\n", 21);
			// redirect output to socket
			dup2(socket, STDOUT_FILENO);
			execvp(exechars[0], exechars);
			exit(1);

		} else {
			// in the parent
			pid_t child = waitpid(pid, NULL, 0 ); // wait for process
		}
	} else if (strcmp(docpath, "/") == 0) {
		//if only "/"
		strcat(cwd, "/http-root-dir/htdocs/index.html");
	} else {
		strcat(cwd, "/http-root-dir/htdocs");
		strcat(cwd, docpath);
	}

	//expand
	if (strstr(docpath, "..") != 0) {
		char resolved[1024];
		memset(resolved, 0, 1024);
		char *res = realpath(docpath, resolved);

		if (res) {
			if (strlen(resolved) >= strlen(cwd) + 21) {
				strcpy(cwd, resolved);
			}
		}
	}

	printf("cwd = %s\n", cwd);



	if (strstr(cwd, "cgi") != NULL) {

	}

	//Determine Content type
	char contentType[1024];
	memset(contentType, 0, sizeof(contentType));
	if (strstr(cwd, ".html") != 0) {
		strcpy(contentType, "text/html");
	} else if (strstr(cwd, ".gif") != 0) {
		strcpy(contentType, "image/gif");
	} else {
		strcpy(contentType, "text/plain");
	}

	FILE * file;
	if (strstr(contentType, "image") != 0) {
		file = fopen(cwd, "rb");
	} else {
		file = fopen(cwd, "r+");
	}


	if (file <= 0 || file == NULL) {
		//not found the file
		write(socket, notfound, strlen(notfound));
	} else { //exist
		int fd = fileno(file);
		if (fd == -1) {
			write(socket, notfound, strlen(notfound));
			fclose(file);
		} else {
			write(socket, "HTTP/1.0 200 Document follows", 29);
			write(socket, "\r\n", 2);
			write(socket, "Server : CS252 Lab5", 19);
			write(socket, "\r\n", 2);
			write(socket, "Content-Type: ", 14);
			write(socket, contentType, strlen(contentType));
			write(socket, "\r\n", 2);
			write(socket, "\r\n", 2);

			int count = 0;
			char c;
			// read the file
			while (count = read(fd, &c, sizeof(c))) {
				if (write(socket, &c, sizeof(c)) != count) {
					perror("write");
				}
				//write(socket, &c, sizeof(c));
			}
			fclose(file);
		}
	}
}
