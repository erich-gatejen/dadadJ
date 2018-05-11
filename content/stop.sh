#!/bin/sh

if [ -z "$DADAD_HOME" ] ; then 
	 echo "FAULT: DADAD_HOME is not set." ; exit 999	 
fi

. $DADAD_HOME/script/system.sh
. $DADAD_HOME/script/api/api.sh

dadad_api_server_stop

while true ; do

	dadad_api_ping
	if [ $? -ne 0 ] ; then
		break
	fi

	echo "... waiting for server to stop responding."
	sleep 5
	
done

while true ; do

	ps -Af  | grep java | grep dadadJ.jar | grep -q dadad.system.data.boot.BootKernel
	if [ $? -ne 0 ] ; then
		break
	fi

	echo "... waiting for server process to stop."
	sleep 5
	
done

exit 0
