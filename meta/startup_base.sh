#!/bin/sh

cd /dadadJ
tar xzf dadadJ.tgz

cd /dadadJ/
. setup.sh
./start.sh

. $DADAD_HOME/script/system.sh
. $DADAD_HOME/script/api/api.sh

while true ; do 
	
	sleep 5
	
	dadad_api_ping
	if [ $? -ne 0 ] ; then
		echo "INFO[0] Server has stopped.  Exiting container."
		exit 0
	fi
	
done

