/*
 * CS354: Operating Systems.
 * Purdue University
 * Example that shows how to read one line with simple editing
 * using raw terminal.
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <termios.h>

#define MAX_BUFFER_LINE 2048

// Buffer where line is stored
int line_length;
char * line_buffer;
//char line_buffer[MAX_BUFFER_LINE];
int position;
void tty_raw_mode(void);

// Simple history array
// This history does not change.
// Yours have to be updated.
int history_index = 0;
char **history = NULL;
int history_length = 0;
int HISTORY_TOTAL = 10;
/*char * usage [] = {
  "ls -al | grep x",
  "ps -e",
  "cat read-line-example.c",
  "vi hello.c",
  "make",
  "ls -al | grep xxx | grep yyy"
};*/
//int history_length = sizeof(history)/sizeof(char *);


void read_line_print_usage()
{
  const char * usage = "\n"
                       " ctrl-?       Print usage\n"
                       " Backspace    Deletes last character\n"
                       " ctrl-D       Removes the character at the cursor.\n"
                       " ctrl-A       The cursor moves to the beginning of the line\n"
                       " ctrl-E       The cursor moves to the end of the line\n";


  write(1, usage, strlen(usage));
}

/*
 * Input a line with some basic editing.
 */
char * read_line() {
  // Set terminal in raw mode
  struct termios orig_attr;
  tcgetattr(0, &orig_attr);
  tty_raw_mode();
  line_length = 0;
  position = 0;
  line_buffer = (char*)calloc(MAX_BUFFER_LINE, sizeof(char));

  if (history == NULL) {
    history = (char**)malloc(HISTORY_TOTAL * sizeof(char*));
  }

  // Read one line until enter is typed
  while (1) {

    // Read one character in raw mode.
    char ch; // the char that readin
    read(0, &ch, 1);

    if (ch >= 32 && ch < 127) {
      // It is a printable character.

      // Do echo
      write(1, &ch, 1);

      // If max number of character reached return.
      if (line_length == MAX_BUFFER_LINE - 2) break;

      if (position != line_length && line_length != 0) { //the position of cursor is in the line.
        //need to move all the char behind the position to right.
        int i;
        line_length++;

        for (i = line_length; i > position; i--) {
          line_buffer[i] = line_buffer[i - 1];
        }

        line_buffer[position] = ch; //char at position, add.

        for (i = position + 1; i < line_length; i++) {
          ch = line_buffer[i];
          write(1, &ch, 1);
        }

        //back the cursor
        ch = 8;
        for (i = line_length; i > position + 1; i--) {
          write(1, &ch, 1);
        }

      } else { //the cursor is at the end
        // just add char to buffer.
        line_buffer[line_length] = ch;
        line_length++;
      }

      position++;

    } else if (ch == 10) {
      // <Enter> was typed. Return line

      // Print newline
      write(1, &ch, 1);

      //make the history
      if (HISTORY_TOTAL == history_length) { //enlarge
        HISTORY_TOTAL = HISTORY_TOTAL * 2;
        history = (char**)realloc(history, HISTORY_TOTAL * sizeof(char*));
      }

      if (line_length != 0) { //have history, add it to history.
        int i;
        int temp = line_length;
        for(i = position; i < temp; i++){
          if(line_buffer[i] == ' '){
            line_length--;
          }
        }
        line_buffer[line_length] = '\0';
        history[history_length] = strdup(line_buffer);
        //printf("length = %d\n", strlen(line_buffer));
        //printf("history = %s\n", history[history_length]);
        history_length++;
      }

      history_index = 0;
      //line_length = 1; //reset line
      //line_buffer = (char*)calloc(0,sizeof(char));

      break;

    } else if (ch == 31) {
      // ctrl-?
      read_line_print_usage();
      line_buffer[0] = 0;
      break;

    } else if ((ch == 8 || ch == 127) && line_length != 0 && position != 0) {
      // <backspace> was typed. Remove previous character read.
      // backspace (ctrl-H)key.  Removes the character at the position before the cursor.

      // Go back one character
      ch = 8;
      write(1, &ch, 1);

      // Write a space to erase the last character read
      //ch = ' ';
      //write(1, &ch, 1);

      // Go back one character
      //ch = 8;
      //write(1, &ch, 1);
      int i;
      for (i = position - 1; i < line_length - 1; i++) {
        line_buffer[i] = line_buffer[i + 1];
        ch = line_buffer[i];
        write(1, &ch, 1);
      }

      line_buffer[line_length - 1] = ' ';
      ch = ' ';
      write(1, &ch, 1);
      ch = 8;
      write(1, &ch, 1);
      line_length--;
      position--;
      //printf("position = %d\n", position);

      for (i = line_length; i > position; i--) {
        ch = 8;
        write(1, &ch, 1);
      }


      // Remove one character from buffer
      //the cursor position --

    } else if (ch == 4 && line_length != 0) {
      //delete key(ctrl-D): Removes the character at the cursor.
      int i;

      for (i = position; i < line_length - 1; i++) {
        line_buffer[i] = line_buffer[i + 1];
        ch = line_buffer[i];
        write(1, &ch, 1);
      }
      ch = ' ';
      write(1, &ch, 1);
      ch = 8;
      write(1, &ch, 1);
      line_buffer[line_length - 1] = ' '; //erase the last character
      line_length--;

      //move back the cursor
      for (i = line_length; i > position; i--) {
        ch = 8;
        write(1, &ch, 1);
      }

    } else if (ch == 1 && line_length != 0) {
      //Home key (or ctrl-A): The cursor moves to the beginning of the line
      int i;

      for (i = position; i > 0; i--) {
        ch = 8; //backspace
        write(1, &ch, 1);
        position--;
      }

    } else if (ch == 5 && line_length != 0) {
      //End key (or ctrl-E): The cursor moves to the end of the line
      int i;
      for (i = position; i < line_length; i++) {
        ch = line_buffer[i];
        write(1, &ch, 1);
        position++;
      }

    } else if (ch == 27) {
      // Escape sequence. Read two chars more
      //
      // HINT: Use the program "keyboard-example" to
      // see the ascii code for the different chars typed.
      //
      char ch1;
      char ch2;
      read(0, &ch1, 1);
      read(0, &ch2, 1);
      if (ch1 == 91 && ch2 == 65 && history_length != 0) { //have history
        // Up arrow. Print previous line in history.

        // Erase old line
        // Print backspaces
        if (history_index < history_length) {
          //printf("history_index = %d\n", history_index);
          int i = 0;
          for (i = position; i > 0; i--) {
            ch = 8;
            write(1, &ch, 1);
          }

          // Print spaces on top
          for (i = 0; i < line_length; i++) {
            ch = ' ';
            write(1, &ch, 1);
          }

          // Print backspaces
          for (i = 0; i < line_length; i++) {
            ch = 8;
            write(1, &ch, 1);
          }
          line_length = 0;
          position = 0;

          // Copy line from history
          history_index = (history_index + 1) % (history_length + 1);
          strcpy(line_buffer, history[history_length - history_index]);
          line_length = strlen(line_buffer);
          //printf("line_length = %d\n", line_length);

          for (i = 0; i < line_length; i++) {
            ch = line_buffer[i];
            write(1, &ch, 1);
          }
          position = line_length; //update the cursor
        }

      } else if (ch1 == 91 && ch2 == 66) {
        //down arrow, print next in the history
        //printf("history_index = %d\n", history_index);
        if (history_index > 0) {
          int i = 0;
          for (i = position; i > 0; i--) {
            ch = 8;
            write(1, &ch, 1);
          }

          for (i = 0; i < line_length; i++) {
            ch = ' ';
            write(1, &ch, 1);
          }

          // Print backspaces
          for (i = 0; i < line_length; i++) {
            ch = 8;
            write(1, &ch, 1);
          }

          line_length = 0;
          position = 0;

          if (history_index > 1) { //in the history
            history_index = (history_index - 1) % (history_length + 1); //update
            strcpy(line_buffer, history[history_length - history_index]);
            line_length = strlen(line_buffer);
            for (i = 0; i < line_length; i++) {
              ch = line_buffer[i];
              write(1, &ch, 1);
            }
            position = line_length; //update the cursor
          } else { //history_index = 1, have an empty line
            strcpy(line_buffer, "");
            line_length = strlen(line_buffer);
            for (i = 0; i < line_length; i++) {
              ch = line_buffer[i];
              write(1, &ch, 1);
            }
            position = line_length;
            history_index = 0;
          }
        }

      } else if (ch1 == 91 && ch2 == 68) {
        //left arrow
        if (position > 0) { //at beginning do nothing, else move
          ch = 8;
          write(1, &ch, 1);
          position--;
        }

      } else if (ch1 == 91 && ch2 == 67) {
        if (position < line_length) { //at the end, do nothing, else move
          ch = line_buffer[position];
          write(1, &ch, 1);
          position++;
        }
      }
    }
  }

  // Add eol and null char at the end of string
  line_buffer[line_length] = 10;
  line_length++;
  line_buffer[line_length] = 0;

  return line_buffer;
}

