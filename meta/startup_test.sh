#!/bin/sh

cd /dadadJ
tar xzf dadadJtest.tgz

cd /dadadJ/
. setup.sh

cd test
./suite.sh DC1

./junitresult.sh DC1 > RESULTS/JUNIT.RESULT

if [ "$DADAD_WAIT" = "DADAD_WAIT" ] ; then
		sleep 2000
fi


