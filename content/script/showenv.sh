#!/bin/sh

if [[ -z "$DADAD_ROOT" ]]; then 
	 echo "FAULT: DADAD_ROOT is not set.  The script berzerq2_setenv_bash.sh should have been" ; exit 999	 
fi

source $DADAD_HOME/script/system.sh

CURRENT_SERVER_LOG=`cat $DADAD_CURRENT_LOGFILE_FILE`

echo "DADAD_HOME=$DADAD_HOME"
echo "DADAD_TEMP_DIR=$DADAD_TEMP_DIR"
echo "DADAD_REST_PORT=$DADAD_REST_PORT"

echo "Current Server Logfile: $CURRENT_SERVER_LOG"



