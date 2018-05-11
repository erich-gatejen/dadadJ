#!/bin/sh

if [ -z "$DADAD_REST_PORT" ] ; then 
	 echo "FAULT: DADAD_REST_PORT is not set.  You probably need to source system.sh before api/api.sh" ; exit 999	 
fi

# ## API FUNCTIONS ## 

# PING
# Return 0 if ping successful
dadad_api_ping () {
	curl -s "http://localhost:$DADAD_REST_PORT/SERVER/PING/TEXT?text=borkitybork" | grep -q borkitybork
}
  
# SET CONFIGURATION VALUE
# Param 1: name of the value
# Param 2: value of the value
# Param 3: OPTIONAL return value check.  Use if substitution on the value is done in the server.
# Return 0 if successful
dadad_api_set () {
	if [ "$#" -lt 2 ] ; then
	    echo "ERROR[9] api_set not enough parameters"
	    return 9
	fi
	
	if [ "$#" -lt 3 ] ; then
	    curl -s --data-urlencode "name=$1" --data-urlencode "value=$2" http://localhost:$DADAD_REST_PORT/SERVER/SET/TEXT  | grep -q "$2"
	    return #?
	else    
	    curl -s --data-urlencode "name=$1" --data-urlencode "value=$2" http://localhost:$DADAD_REST_PORT/SERVER/SET/TEXT  | grep -q "$3"
	    return #?	    
	fi
			
}

# SET CONFIGURATION VALUE # exit if there is an error
# Param 1: name of the value
# Param 2: value of the value
# Param 3: OPTIONAL return value check.  Use if substitution on the value is done in the server.
# Return 0 if successful otherwise EXIT
dadad_api_set_errorexit () {
	dadad_api_set "$1" "$2" "$3"
	if [ "$?" -ne 0 ] ; then
	    exit $?
	fi
}

# LOAD PROPS
# Param 1: path to file, either full or from DADAD_HOME
# Return 0 if successful, EXIT otherwise.
dadad_api_loadprop () {
	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_api_loadprop not enough parameters"
	    return 9
	fi
	
	curl -s "http://localhost:$DADAD_REST_PORT/SERVER/LOAD/TEXT?path=$1" | grep -q "$1"
	if [ $? -ne 0 ] ; then
		echo "FAULT[99] SERVER DID NOT LOAD PROPERTIES.  Check server log."
		exit 99
	fi
}

# SAVE PROPS
# Param 1: full path to file 
# Return 0 if successful, EXIT otherwise.
dadad_api_saveprop () {
	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_api_saveprop not enough parameters"
	    return 9
	fi
	
	curl -s "http://localhost:$DADAD_REST_PORT/SERVER/PROPERTIES/TEXT" >"$1"
	if [ $? -ne 0 ] ; then
		echo "FAULT[99] SERVER DID NOT SAVE PROPERTIES TO FILE $1.  Check server log."
		exit 99
	fi
}

# SAVE WORKPROCESS PROPS
# Param 1: full path to file
# Param 2: wp name 
# Return 0 if successful, EXIT otherwise.
dadad_api_savewpprop () {
	if [ "$#" -lt 2 ] ; then
	    echo "ERROR[9] dadad_api_savewpprop not enough parameters"
	    return 9
	fi
	
	curl -s "http://localhost:$DADAD_REST_PORT/SERVER/WPPROPERTIES/TEXT?name=$2" >"$1"
	grep -q "API call failed" "$1" 
	if [ $? -eq 0 ] ; then
		echo "FAULT[99] SERVER DID NOT SAVE PROPERTIES TO FILE $1 for WP $2.  Check server log."
		exit 99
	fi
}

# START
# Param 1: Classname to start
# Return 0 if successful.  It will export DADADAPI_LAST_START_NAME as the name of the process.  Otherwise it will EXIT.
dadad_api_start () {
	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_start not enough parameters"
	    return 9
	fi
	
	RESULT=`curl -s "http://localhost:$DADAD_REST_PORT/SERVER/START/TEXT?classname=$1"`
	echo $RESULT | grep -q "WP"
	if [ $? -ne 0 ] ; then
		echo "FAULT[99] SERVER DID NOT ACCEPT START WORKFLOW.  Check server log."
		exit 99
	fi
	
	export DADADAPI_LAST_START_NAME="$RESULT"
}

# GET PROC OVERALL STATE INCLUDING RESULT
# Param 1: name of process
# Return 0 if successful.  It will export DADADAPI_LAST_STATE as the state of the process.  Otherwise it will EXIT.
dadad_api_get_state () {
	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_api_get_state not enough parameters"
	    return 9
	fi
	
	RESULT=`curl -s "http://localhost:$DADAD_REST_PORT/SERVER/STATE/TEXT?name=$1"`
	echo $RESULT | grep -q "state="
	if [ $? -ne 0 ] ; then
		echo "ERROR[9] Process state not known."
		exit 9
	fi
	
	export DADADAPI_LAST_STATE="$RESULT"
}

# GET PROC SPECIFIC STATE TOKEN
# Param 1: name of process
# Return 0 if successful.  It will export DADADAPI_LAST_STATE_TOKEN as the state of the process.  Otherwise it will EXIT.
dadad_api_get_state_token () {
	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_api_set_state not enough parameters"
	    return 9
	fi

	dadad_api_get_state $1
	TOKEN=`printf "%s" "$DADADAPI_LAST_STATE" | grep "state=" | cut -d'=' -f2`
	export DADADAPI_LAST_STATE_TOKEN="$(echo "${TOKEN}" | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')"
	
}

# STOP SERVER
# Failure exits script
dadad_api_server_stop () { 

	curl -s "http://localhost:$DADAD_REST_PORT/SERVER/STOP/TEXT?text=borkitybork" | grep -q borkitybork
	if [ $? -ne 0 ] ; then
		echo "FAULT[99] SERVER DID NOT ACCEPT STOP REQUEST.  Check server log."
		exit 99
	fi
}

# GET REPORT LOG URL
# Param 1: name of process
# Return 0 always.  It will export DADADAPI_LAST_REPORT_LOG_URL as the url to the report log or an empty string if the url is not known or there was an error
dadad_api_report_log_url () { 

	if [ "$#" -lt 1 ] ; then
	    echo "ERROR[9] dadad_api_report_log_url not enough parameters"
	    return 9
	fi
	
	RESULT=`curl -s "http://localhost:$DADAD_REST_PORT/SYSTEM/REPORTLOG/TEXT?work.process.name=$1"`
	export DADADAPI_LAST_REPORT_LOG_URL="$RESULT"

}