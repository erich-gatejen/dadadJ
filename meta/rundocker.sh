#!/bin/sh

# param #1 - Name
# param #2 - OPTIONAL Server port 
# param #3 - OPTIONAL PROD or TEST
# param #4 - OPTIONAL WAIT - Wait 2000 seconds at the end.  Useful for testing. 


# Change to your hup repo if you plan on pushing this.  Please don't use mine.  :^)
REPO_NAME=vadrick

if [ "$#" -lt 2 ]; then
    echo "FAULT[99] Must specify a name and host port"
    exit 99
fi

DADAD_PORT=8585
DADAD_NAME="dadadjbase_$1"

DADAD_PURPOSE="PROD"
DADAD_IMAGE="base"
if [ "$#" -gt 2 ]; then
    
	case "$3" in
    	PROD) 	
    		;;
    	
        TEST)
        	DADAD_PURPOSE="TEST"
        	DADAD_IMAGE="test"
        	;;
        	
        *)
        	echo "FAULT[99] Unknown purpose tag = $2.  It must be PROD or TEST."
        	exit 99
        	;;  
     	        
    esac
    
fi

DADAD_WAIT="DADAD_DONT_WAIT"
if [ "$4" = "WAIT" ] ; then
		DADAD_WAIT="DADAD_WAIT"	
fi

docker run -d -t -i \
	-e DADAD_PURPOSE="$DADAD_PURPOSE" \
	-e DADAD_WAIT="$DADAD_WAIT" \
	-p "$2:$DADAD_PORT" \
	--name "$DADAD_NAME" "$REPO_NAME/dadadj$DADAD_IMAGE"

echo "$DADAD_NAME"

