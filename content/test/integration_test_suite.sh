#!/bin/sh

# You can pass DEBUGGER as the second param to start server in debugger mode.
# You can pass log level as the third param.  The default is DEBUG.

## SETUP     ###################################################################

./starttesting.sh $1 $2 $3
if [ $? -ne 0 ] ; then
	echo "Could not start test environment."
	exit 9
fi


## EXECUTION     #################################################################

./run.sh csv
./run.sh json


## SHUTDOWN     #################################################################

./endtesting.sh $1
if [ $? -ne 0 ] ; then
	echo "Could not stop test environment."
	exit 9
fi

exit 0

