#include <stdio.h>

int
isLittleEndian() {
/*write your code here*/
	int a = 0x04;
	char * p = (char *) &a;
	if (*p==0x04) {
 	return 1;
	}
	
 return 0;
}

int main()
{
  if (isLittleEndian()) {
    printf("Machine is Little Endian\n");
  }
  else {
    printf("Machine is Big Endian\n");
  }
}
