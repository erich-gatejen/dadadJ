#!/bin/sh

ant test 
if [ $? -ne 0 ] ; then
	echo "FAULT[9] Build failed."
		exit 9	
fi

_VERSION=`cat VERSION`

mkdir -p packages

cd dist
tar czf ../meta/dadadJ-${_VERSION}.tgz ../../packages
cd ../test
tar czf ../meta/dadadJtest-${_VERSION}.tgz ../../packages
cd ../lib
cp dadadJ.jar ../../packages/dadadJ-${_VERSION}.jar
