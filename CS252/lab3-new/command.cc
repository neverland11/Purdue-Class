
/*
 * CS252: Shell project
 *
 * Template file.
 * You will need to add more code here to execute the command table.
 *
 * NOTE: You are responsible for fixing any bugs this code may have!
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>
#include <signal.h>
#include <fcntl.h>
#include "command.h"
#include <pwd.h>

extern char **environ; 


SimpleCommand::SimpleCommand()
{
	// Create available space for 5 arguments
	_numOfAvailableArguments = 5;
	_numOfArguments = 0;
	_arguments = (char **) malloc( _numOfAvailableArguments * sizeof( char * ) );
}

void
SimpleCommand::insertArgument( char * argument )
{
	if ( _numOfAvailableArguments == _numOfArguments  + 1 ) {
		// Double the available space
		_numOfAvailableArguments *= 2;
		_arguments = (char **) realloc( _arguments,
				  _numOfAvailableArguments * sizeof( char * ) );
	}
	
	//expand arguments
	char * expandArg = (char*)malloc(1024 * sizeof(char));
	char * strName = (char*)malloc(1024 * sizeof(char));
	char * strValue = (char*)malloc(1024 * sizeof(char));
	int strcount = 0;
	int totalcount = 0;
	int i = 0;

	while(argument[i] != '\0'){
		if (argument[i] == '$' && argument[i + 1] == '{') {
			i = i + 2; //move forward $ and {
			while (argument[i] != '}') {
				strName[strcount] = argument[i];
				strcount++;
				i++;
			}
			
			strName[strcount] = '\0';
			strValue = getenv(strName);
			strcat(expandArg, strValue); 

			totalcount = totalcount + strlen(strValue);
			memset(strName, 0, sizeof(strName));
			memset(strValue, 0, sizeof(strValue));
			strcount = 0;		
		}
		else {
			expandArg[totalcount] = argument[i];
			expandArg[totalcount + 1] = '\0';
			totalcount++;
		}
		i++;
	}


	//tilde expansion
	if(expandArg[0] == '~'){
		char * home = (char*)malloc(1024 * sizeof(char));
		if(strchr(expandArg, '/') == NULL){
			expandArg++;
			home = expandArg;
			expandArg--;
		} else {
			int i = 1;
			while(expandArg[i] != '/'){
				home[i - 1] = expandArg[i];
				i++;
			}
		}

		struct passwd * pw;
		if(!strcmp(home, "")){ //same
			pw = getpwuid(getuid());
		} else {
			pw = getpwnam(home);
		}

		const char * dir = pw->pw_dir;
		char * resArg = (char*)malloc((strlen(expandArg) + strlen(dir)) * sizeof(char));
		resArg[0] = '\0';
		strcat(resArg, dir);
		strcat(resArg, "/");
		strcat(resArg, (char*)(expandArg + 1 + strlen(home)));

		expandArg = resArg;
	}



	// Add NULL argument at the end
	_arguments[_numOfArguments] = strdup(expandArg);
	//_arguments[_numOfArguments] = strdup(argument);
	_arguments[_numOfArguments + 1] = NULL;
	_numOfArguments++;
}

Command::Command()
{
	// Create available space for one simple command
	_numOfAvailableSimpleCommands = 1;
	_simpleCommands = (SimpleCommand **)
		malloc( _numOfSimpleCommands * sizeof( SimpleCommand * ) );

	_numOfSimpleCommands = 0;
	_outFile = 0;
	_inFile = 0;
	_errFile = 0;
	_background = 0;
}

void
Command::insertSimpleCommand( SimpleCommand * simpleCommand )
{
	if ( _numOfAvailableSimpleCommands == _numOfSimpleCommands ) {
		_numOfAvailableSimpleCommands *= 2;
		_simpleCommands = (SimpleCommand **) realloc( _simpleCommands,
			 _numOfAvailableSimpleCommands * sizeof( SimpleCommand * ) );
	}
	
	_simpleCommands[ _numOfSimpleCommands ] = simpleCommand;
	_numOfSimpleCommands++;
}

void
Command:: clear()
{
	for ( int i = 0; i < _numOfSimpleCommands; i++ ) {
		for ( int j = 0; j < _simpleCommands[ i ]->_numOfArguments; j ++ ) {
			free ( _simpleCommands[ i ]->_arguments[ j ] );
		}
		
		free ( _simpleCommands[ i ]->_arguments );
		free ( _simpleCommands[ i ] );
	}

	if ( _outFile ) {
		free( _outFile );
	}

	if ( _inFile ) {
		free( _inFile );
	}

	if ( _errFile ) {
		free( _errFile );
	}

	_numOfSimpleCommands = 0;
	_outFile = 0;
	_inFile = 0;
	_errFile = 0;
	_background = 0;
}

void
Command::print()
{
	printf("\n\n");
	printf("              COMMAND TABLE                \n");
	printf("\n");
	printf("  #   Simple Commands\n");
	printf("  --- ----------------------------------------------------------\n");
	
	for ( int i = 0; i < _numOfSimpleCommands; i++ ) {
		printf("  %-3d ", i );
		for ( int j = 0; j < _simpleCommands[i]->_numOfArguments; j++ ) {
			printf("\"%s\" \t", _simpleCommands[i]->_arguments[ j ] );
		}
	}

	printf( "\n\n" );
	printf( "  Output       Input        Error        Background\n" );
	printf( "  ------------ ------------ ------------ ------------\n" );
	printf( "  %-12s %-12s %-12s %-12s\n", _outFile?_outFile:"default",
		_inFile?_inFile:"default", _errFile?_errFile:"default",
		_background?"YES":"NO");
	printf( "\n\n" );
	
}

void
Command::execute()
{
	// Don't do anything if there are no simple commands
	if ( _numOfSimpleCommands == 0 ) {
		prompt();
		return;
	}

	// Print contents of Command data structure
	//print();

	// Add execution here
	// For every simple command fork a new process
	// Setup i/o redirection
	// and call exec

	int defaultin = dup(0);
	int defaultout = dup(1);
	int defaulterr = dup(2);
	int fdin;
	int fdout;
	int fderr;

	if(_inFile){ //if exists
		fdin = open(_inFile, O_RDONLY, 0600);
	} else {
		fdin = dup(defaultin);
	}

	int fdpipe[2]; //0 for read, 1 for write
	int pid;


	for(int i = 0; i < _numOfSimpleCommands; i++){
		dup2(fdin, 0); //input
		close(fdin); //no longer use

		//the last command
		if(i == _numOfSimpleCommands - 1){
			if(_outFile){ //if there has output/err
				if(_append == 1){
					fdout = open(_outFile, O_WRONLY|O_CREAT|O_APPEND, 0660);
				} else {
					fdout = open(_outFile, O_WRONLY|O_CREAT|O_TRUNC, 0660);
				}
			} else {
				fdout = dup(defaultout);
			}

			if(_errFile){ //if there has output/err
				if(_append == 1){
					fderr = open(_errFile, O_WRONLY|O_CREAT|O_APPEND, 0660);
				} else {
					fderr = open(_errFile, O_WRONLY|O_CREAT|O_TRUNC, 0660);
				}
			} else {
				fderr = dup(defaulterr);
			}

			dup2(fderr, 2);
			close(fderr);

		} else { //not the last

			if(pipe(fdpipe) == -1){
				perror("pipe\n");
				exit(2);
			}

			fdout = fdpipe[1];
			fdin = fdpipe[0];
		}

		dup2(fdout, 1);
		close(fdout);

		if(!strcmp(_simpleCommands[i]->_arguments[0], "exit")){
			//printf("Good bye!!\n");
			exit(0);
		}

		//set the environment variables
		if (!strcmp( _simpleCommands[i]->_arguments[0], "setenv")){
			// add your code to set the environment variable
			setenv(_simpleCommands[i]->_arguments[1], _simpleCommands[i]->_arguments[2], 1); //change name to value. 1>0
			continue;
		}

		if(!strcmp(_simpleCommands[i]->_arguments[0], "unsetenv")){
			unsetenv(_simpleCommands[i]->_arguments[1]);
			continue;
		}

		if(!strcmp(_simpleCommands[i]->_arguments[0], "cd")){
			int cd;
			if (_simpleCommands[0]->_arguments[1] == NULL) { //to the home directory
				cd = chdir(getenv("HOME"));
			} else {
				cd = chdir(_simpleCommands[0]->_arguments[1]);
			}
			if (cd != 0) {
				perror("chdir");
			}
			continue;
		}

		pid = fork();

		if(pid == -1){
			perror("fork error\n");
			exit(2);
		}

		if(pid == 0){ //if is the child process
			if(!strcmp(_simpleCommands[i]->_arguments[0], "printenv")){ //print
				char **p = environ;
				while (*p != NULL) {
					printf("%s\n", *p);
					p++;
				}
				exit(0);
			}
			execvp(_simpleCommands[i]->_arguments[0], _simpleCommands[i]->_arguments);
			perror("execvp error\n");
			exit(2);
		}
	}

	if(_background != 1){ //no background
		waitpid(pid, NULL, 0);
	}

	dup2(defaultin, 0);
	dup2(defaultout, 1);

	// Clear to prepare for next command
	clear();
	
	// Print new prompt
	prompt();
}

// Shell implementation

void
Command::prompt()
{
	if(isatty(0)){
		printf("myshell>");
		fflush(stdout);
	}
}

extern "C" void disp(int sig)
{
	printf("\n");
	Command::_currentCommand.prompt();
}

extern "C" void zombie(int sig)
{
	while(waitpid(-1, NULL, WNOHANG) > 0);
}

Command Command::_currentCommand;
SimpleCommand * Command::_currentSimpleCommand;

int yyparse(void);

int main()
{
	struct sigaction sa;
	sa.sa_handler = disp;
	sigemptyset(&sa.sa_mask);
	sa.sa_flags = SA_RESTART; //restart the system calls
	
	if(sigaction(SIGINT, &sa, NULL)){
        perror("sigaction");
        exit(2);
    }

    struct sigaction sa_zombie;
    sa_zombie.sa_handler = zombie;
    sigemptyset(&sa_zombie.sa_mask);
    sa_zombie.sa_flags = SA_RESTART;
    if (sigaction(SIGCHLD, &sa_zombie, NULL)) {
		perror ("sigchild");
	}

    Command::_currentCommand.prompt();
	yyparse();
}

