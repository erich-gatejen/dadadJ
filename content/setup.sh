#!/bin/sh

# Is mac
UNAMEV=`uname`
if [ "$UNAMEV" = DARWIN ]; then	

	# yes - hax.  Prolly should not make this assumption
	THIS_SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
			
else
	
	# straight sh
	SCRIPT="$(readlink -f $0)"
	THIS_SCRIPT_DIR="$(dirname $SCRIPT)"
	
fi

export DADAD_HOME=$THIS_SCRIPT_DIR
	
