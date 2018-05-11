#!/bin/sh

# You can pass DEBUGGER as the second param to start server in debugger mode.
# You can pass log level as the third param.  The default is DEBUG.

if [ -z "$DADAD_HOME" ] ; then 
	 echo "FAULT: DADAD_HOME is not set." ; exit 999	 
fi

if [ "$#" -lt 1 ]; then
    echo "FAULT[99] Must specify RUN value"
    exit 99
fi

. $DADAD_HOME/script/system.sh
. $DADAD_HOME/script/api/api.sh

export RUN=$1

DADAD_TEST_RESULT_BASEDIR="$DADAD_HOME/test/RESULTS/$RUN"
if [ -d DADAD_TEST_RESULT_BASEDIR ] ; then
	echo "FAULT: run already exists.  Cannot create new result directory at $DADAD_TEST_RESULT_BASEDIR"
	exit 99
fi
mkdir -p $DADAD_TEST_RESULT_BASEDIR

echo "$RUN" > $DADAD_TEMP_DIR/CURRENT_RUN

cd $DADAD_HOME
./start.sh $2 $3 $4 $5
if [ $? -ne 0 ] ; then
	echo "FAULT[99] SERVER DID NOT START.  Check $SERVER_LOG_FILE" ; exit 99
fi

dadad_api_set run "$RUN"
if [ $? -ne 0 ] ; then
	echo "FAULT[99] COULD NOT SET RUN VALUE.  Check $SERVER_LOG_FILE" ; exit 99
fi

exit 0

