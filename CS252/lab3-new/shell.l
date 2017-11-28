
/*
 *
 * CS-252 Fall 2013
 * shell.l: lexical analyzer for shell
 * You have to extend it.
 *
 */

%{

#include <string.h>
#include "y.tab.h"
#include <sys/wait.h>
#include <sys/types.h>
#include <unistd.h>

extern "C" char * read_line();

////////////  Start added code ///////////
int mygetc(FILE * f) {
static char *p;
char ch;
if (!isatty(0)) {
  // stdin is not a tty. Call real getc
  return getc(f);
}
// stdin is a tty. Call our read_line.
if (p==NULL || *p == 0) {
  char * s = read_line();
  p = s;
}
ch = *p;
p++;
return ch;
}
#undef getc
#define getc(f) mygetc(f)
/////////// End added code ///////////



static  void yyunput (int c,char *buf_ptr  );

void myunputc(int c) {
    unput(c);
}

%}

%%

\n 	{
		return NEWLINE;
	}

[ \t] 	{
		/* Discard spaces and tabs */
     	}	

">" 	{
		return GREAT;
	}

">>"	{
		return GREATGREAT;
}

"|"		{
		return PIPE;
}

"&"		{
		return AMPERSAND;
}

">&"	{
		return GREATAMPERSAND;
}

">>&"	{
		return GREATGREATAMPERSAND;
}

"<"		{
		return LESS;
}





\"[^\n\"]*\"    {
		//quote
       yytext = (char*)(yytext+1);
       yytext[ strlen(yytext) - 1 ] = '\0';
       yylval.string_val = strdup(yytext);
       return WORD;
   } 

[^ \t\n&<>\|]*\\[^ \t\n]*	{
	//escape
		char * use = (char*)malloc(strlen(yytext) + 1);
		int x = 0;
		for(int i = 0; i < strlen(yytext); i++){
			if(yytext[i] == '\\'){
				i++;
			}
			use[x] = yytext[i];
			x++;
		}
		use[x] = '\0';
		yylval.string_val = strdup(use);
		return WORD;

	}


[^ \t\n|><&][^ \t\n|><&]*  {
		/* Assume that file names have only alpha chars */
		yylval.string_val = strdup(yytext);
		return WORD;
	}

