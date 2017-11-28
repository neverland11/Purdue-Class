#!/bin/bash

#DO NOT REMOVE THE FOLLOWING TWO LINES
git add $0 >> .local.git.out
git commit -a -m "Lab 2 commit" >> .local.git.out

#Your code here
password=`head -1 $1`
COUNT=0

#less than 6 or greater than 32, exit
if [ "${#password}" -lt 6 ]; then
	echo "Error: Password length invalid."
	exit 1
fi

if [ "${#password}" -gt 32 ]; then
	echo "Error: Password length invalid."
	exit 1
fi

#add the score of the length
COUNT=$((COUNT+${#password}))

#echo Password Length: $COUNT
echo $password > outfile.txt 

char=`egrep [#$\+%@] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT+5))
fi

char=`egrep [0-9] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT+5))
fi

char=`egrep [a-zA-Z] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT+5))
fi

char=`egrep [a-z][a-z][a-z] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT-3))
fi

char=`egrep [A-Z][A-Z][A-Z] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT-3))
fi

char=`egrep [0-9][0-9][0-9] outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT-3))
fi

char=`egrep '([a-zA-Z0-9])\1+' outfile.txt`
if [ "${#char}" -gt 0 ]; then
	COUNT=$((COUNT-10))
fi
#if [ "$password"]
#output
echo Password Score: $COUNT