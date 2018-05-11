#!/bin/sh

if [ -z "$DADAD_HOME" ] ; then 
	 echo "FAULT: DADAD_HOME is not set." ; exit 999	 
fi

cd $DADAD_HOME
./stop.sh

exit 0
