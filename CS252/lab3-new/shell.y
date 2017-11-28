
/*
 * CS-252
 * shell.y: parser for shell
 *
 * This parser compiles the following grammar:
 *
 *	cmd [arg]* [> filename]
 *
 * you must extend it to understand the complete shell grammar
 *
 */

%token	<string_val> WORD

%token 	NOTOKEN GREAT NEWLINE PIPE GREATGREAT AMPERSAND GREATAMPERSAND GREATGREATAMPERSAND LESS

%union	{
		char   *string_val;
	}

%{
//#define yylex yylex
#include <stdio.h>
#include "command.h"
#include <string.h>
#include <dirent.h>
#include <regex.h>
#include <stdlib.h>


void yyerror(const char * s);
int yylex();
void expandWildcard(char * prefix, char * suffix); 


%}

%%

goal:	
	commands
	;

commands: 
	command
	| commands command 
	;

command: simple_command
    ;

simple_command: 
	pipe_list io_modifier_list background_opt NEWLINE {
		//printf("   Yacc: Execute command\n");
		Command::_currentCommand.execute();
	}
	| NEWLINE 
	| error NEWLINE { yyerrok; }
	;

command_and_args:
	command_word argument_list {
		Command::_currentCommand.
			insertSimpleCommand( Command::_currentSimpleCommand );
	}
	;

argument_list:
	argument_list argument
	| /* can be empty */
	;


argument:
	WORD {
               //printf("   Yacc: insert argument \"%s\"\n", $1);
		//if (strchr($1, '*') == NULL && strchr($1, '?') == NULL) { //does not have wildcards
			//Command::_currentSimpleCommand->insertArgument( $1 );
		//} else {
		//}
		if (strchr($1, '*') == NULL && strchr($1, '?') == NULL) {
			Command::_currentSimpleCommand->insertArgument( $1 );
		} else {
			char * temp = (char*)malloc(1 * sizeof(char));
			*temp = '\0';
			expandWildcard(temp, $1);
		}
	}
	;

command_word:
	WORD {
               //printf("   Yacc: insert command \"%s\"\n", $1);
	       
	       Command::_currentSimpleCommand = new SimpleCommand();
	       Command::_currentSimpleCommand->insertArgument( $1 );
	}
	;

pipe_list:
	pipe_list PIPE command_and_args
	| command_and_args
	;

background_opt:
	AMPERSAND {
		Command::_currentCommand._background = 1;
	}
	| /* empty */
	;

io_modifier_list:
	io_modifier_list iomodifier_opt
	| iomodifier_opt 
	| /* empty */ 
	;



iomodifier_opt:
	GREAT WORD {
		//printf("   Yacc: insert output \"%s\"\n", $2);
		if (Command::_currentCommand._outFile){
			printf("Ambiguous output redirect\n");
			exit(0);
		}

		Command::_currentCommand._outFile = $2;
	}

	| GREATGREAT WORD {
		if (Command::_currentCommand._outFile) {
			printf("Ambiguous output redirect\n");
			exit(0);
		}

		Command::_currentCommand._outFile = $2;
		Command::_currentCommand._append = 1;
	}
	| GREATAMPERSAND WORD {
		if (Command::_currentCommand._outFile) {
			printf("Ambiguous output redirect\n");
			exit(0);
		}

		Command::_currentCommand._outFile = $2;
		Command::_currentCommand._errFile = $2;
	}
	| GREATGREATAMPERSAND WORD {
		if (Command::_currentCommand._outFile) {
			printf("Ambiguous output redirect\n");
			exit(0);
		}

		Command::_currentCommand._outFile = $2;
		Command::_currentCommand._errFile = $2;
		Command::_currentCommand._append = 1;
	}
	| LESS WORD {
		Command::_currentCommand._inFile = $2;
	}

	| /* can be empty */ 
	;


%%

void expandWildcard(char * prefix, char * suffix)
{	
	//printf("expandwildcards\n");

	if (suffix[0] == 0) {
		// suffix is empty. Put prefix in argument.
		Command::_currentSimpleCommand->insertArgument(strdup(++prefix));
		return;
	}

	// Obtain the next component in the suffix
	char * s = strchr(suffix, '/');
	char component[1024];
	
	if (s != NULL) {	//Copy up to the first '/'
		strncpy(component, suffix, s - suffix);
		suffix = s + 1;
	} else { // last part of path. Copy the whole thing
		strcpy(component, suffix);
		suffix = suffix + strlen(suffix);
	}


	// expand the component
	char * newPrefix = (char*)calloc(0, 1024 * sizeof(char));

	// if component does not have wildcards
	if (strchr(component, '*') == NULL && strchr(component, '?') == NULL) {
		sprintf(newPrefix, "%s/%s", prefix, component);
		//printf("newPrefix = %s, suffix = %s\n", newPrefix, suffix);
		expandWildcard(newPrefix, suffix); 
		return; 
	}

	char * reg = (char *)malloc(2*strlen(component)+10);
	char * a   = component;
	char * r   = reg;
	//printf("component = %s\n", component);
	*r = '^'; r++; // match beginning of line
	while (*a) {
		if (*a == '*') { *r = '.'; r++; *r = '*'; r++; } //*->.*
		else if (*a == '?') { *r = '.'; r++; }
		else if (*a == '.') { *r = '\\'; r++; *r = '.'; r++; }
		else { *r = *a; r++; }
		a++;
	}
	*r='$'; 
	r++; 
	*r='\0';

	// compile the regex
	regex_t re;
	int result = regcomp(&re, reg, REG_EXTENDED|REG_NOSUB);
	if (result != 0) { //fail
		perror("regex compile");
		exit(1);
	}

	char * dir;
	int n;
	struct dirent ** dirlist;

	//printf("prefix = %s\n", prefix);

	if (prefix[0] == 0 || prefix == NULL){
		n = scandir(".", &dirlist, 0, alphasort);
	}
	else {
		n = scandir(prefix, &dirlist, 0, alphasort);
	}

	if(n < 0){
		return;
	}

	int count = 0;
	struct dirent * ent;
	while(count < n){
		ent = dirlist[count];
		if (regexec(&re, ent->d_name, 0, NULL, 0) == 0) {
			//printf("name = %s\n", ent->d_name);
			if (prefix == NULL || prefix[0] == 0) {
				//printf("newPrefix = %s\n", newPrefix);
				sprintf(newPrefix, "/%s", ent->d_name); //add names
			} else {
				//printf("newPrefix = %s\n", newPrefix);
				sprintf(newPrefix, "%s/%s", prefix, ent->d_name);
			}

			if (ent->d_name[0] == '.') {
				if (component[0] == '.') {
					//printf("newPrefix = %s, suffix = %s\n", newPrefix, suffix);
					expandWildcard(newPrefix, suffix);
				}
			} else {
				//printf("newPrefix = %s, suffix = %s\n", newPrefix, suffix);
				expandWildcard(newPrefix, suffix);
			}
		}
		count++;
	}
}


void
yyerror(const char * s)
{
	fprintf(stderr,"%s", s);
}

#if 0
main()
{
	yyparse();
}
#endif
