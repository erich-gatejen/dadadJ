#!/bin/sh

if [ -z "$DADAD_HOME" ] ; then 
	 echo "FAULT: DADAD_HOME is not set." ; exit 999	 
fi

. $DADAD_HOME/script/system.sh
. $DADAD_HOME/script/api/api.sh

export DEBUG_PORT=6666
export DEBUG_LEVEL=INFO

SERVER_LOG_FILE="$DADAD_HOME/log/SERVER_$DADAD_DATE.log"
echo "$SERVER_LOG_FILE" > "$DADAD_CURRENT_LOGFILE_FILE"

# Roll logs if there are some there
OLD_LOG_NAME=`ls -1 $DADAD_HOME/log | grep SERVER_ | cut -c 8-22`
if [ ! -z "$OLD_LOG_NAME" ] ; then 
	 mkdir "$DADAD_HOME/log/$OLD_LOG_NAME"
	 find "$DADAD_HOME/log" -maxdepth 1 -type f -name '[!.]*' -exec mv -n {} "$DADAD_HOME/log/$OLD_LOG_NAME" \; 
fi

# Is one already running on this port?
export DADAD_REST_PORT=`grep "system.rest.port=" "$DADAD_HOME/server.prop" | cut -d'=' -f2`
ps -Af  | grep java | grep dadadJ.jar | grep dadad.system.data.boot.BootKernel | grep -q "system.rest.port=$DADAD_REST_PORT"
if [ $? -eq 0 ] ; then
	echo "Server already running on REST port $DADAD_REST_PORT.  See system.rest.port= property in $DADAD_HOME/server.prop. if you are trying to run multiple servers."
	exit 99
fi

export START_LINE="-server"

while [ $# -gt 0 ]
do
    case "$1" in
    	DEBUGGER)
		 	START_LINE="-server -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$DEBUG_PORT"
			
			ps -Af  | grep java | grep agentlib | grep -q "address=$DEBUG_PORT"
			if [ $? -eq 0 ] ; then
				echo "Something is already listen on port $DEBUG_PORT for remote debugging."
				exit 99
			else
				echo "Starting in DEBUGGER mode.  Attach debugger to port $DEBUG_PORT."
			fi   	
    		;;
    	
        TRACE)
        	DEBUG_LEVEL="TRACE"
        	;;
        	
        DEBUG)
        	DEBUG_LEVEL="DEBUG"
        	;;  
        	
    	INFO)
        	DEBUG_LEVEL="INFO"
        	;;        	
        
    esac
    shift
done

java $START_LINE \
  -classpath "$DADAD_HOME/lib/dadadJ.jar" \
  dadad.system.data.boot.BootKernel \
  "$DADAD_HOME" \
  "$DADAD_HOME/server.prop" \
  log.level="$DEBUG_LEVEL" \
  system.rest.port=$DADAD_REST_PORT \
  > "$SERVER_LOG_FILE" 2>&1 </dev/null &

if [ "$START_LINE" == '-server' ] ; then
    sleep 2
    dadad_api_ping
    if [ $? -ne 0 ] ; then
        echo "FAULT[99] SERVER DID NOT START.  Check $SERVER_LOG_FILE"
        exit 99
    fi

else

    loops=0
    while : ; do
        if [ $loops -ge 200 ] ; then
            echo "FAULT[99] SERVER DID NOT START.  Timeout waiting for debugger to attach.  Check $SERVER_LOG_FILE"
            exit 99
        fi
        loops=$[$loops+1]

        dadad_api_ping
        if [ $? -eq 0 ] ; then
            break
        fi

        sleep 1
    done
fi






