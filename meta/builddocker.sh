#!/bin/sh

# Change to your repo if you plan on pushing this.  Please don't use mine.  :^)
REPO_NAME=vadrick

if [ ! -f "dadadJ.tgz" ] ; then
	echo "FAULT[99] Distribution package dadadJ.tgz not built.  Go to root dir and run package.sh"
	exit 99
fi

if [ ! -f "dadadJtest.tgz" ] ; then
	echo "FAULT[99] Test package dadadJtest.tgz not built.  Go to root dir and run package.sh"
	exit 99
fi

docker build -t "$REPO_NAME/dadadjbase" -f Dockerfile.base .
docker build -t "$REPO_NAME/dadadjtest" -f Dockerfile.test .

