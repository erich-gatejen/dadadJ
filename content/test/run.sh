#!/bin/sh

if [ "$#" -lt 1 ] ; then
    echo "FAULT[99] Must specify test directory"
    exit 99
fi

TEST_TICK_LIMIT=100


## SETUP     ###################################################################
. "$DADAD_HOME/script/system.sh"
. "$DADAD_HOME/script/api/api.sh"

export DADAD_TEST_DIR="$DADAD_HOME/test/$1"
if [ ! -d "$DADAD_TEST_DIR" ]; then
    echo "FAULT[99] Test directory does not exist at $DADAD_TEST_DIR"
    exit 99
fi

export DADAD_TEST_LIST="$DADAD_TEST_DIR/test.sh"
if [ ! -f "$DADAD_TEST_LIST" ]; then
    echo "FAULT[99] Test list file does not exist at $DADAD_TEST_LIST"
    exit 99
fi

if [ ! -f $DADAD_TEMP_DIR/CURRENT_RUN ] ; then
    echo "FAULT[99] RUN value not set in $DADAD_TEMP_DIR/CURRENT_RUN.  You must run starttesting.sh before running any test."
    exit 99
fi
export RUN=`cat $DADAD_TEMP_DIR/CURRENT_RUN`

DADAD_TEST_RESULT_BASEDIR="$DADAD_HOME/test/RESULTS/$RUN"
export DADAD_TEST_RESULT_DIR="$DADAD_TEST_RESULT_BASEDIR/$1"
mkdir -p "$DADAD_TEST_RESULT_DIR"

export DADAD_TEST_STATUS_FILE="$DADAD_TEST_RESULT_DIR/STATUS.txt"
export DADAD_TEST_VALIDATION_FILE="$DADAD_TEST_RESULT_DIR/VALIDATION.txt"
export DADAD_TEST_CONFIG_FILE="$DADAD_TEST_RESULT_DIR/CONFIG.prop"

export DADAD_TEST_SETUP="$DADAD_TEST_DIR/setup.sh"
export DADAD_TEST_CLEAN="$DADAD_TEST_DIR/clean.sh"
export DADAD_TEST_VALIDATE="$DADAD_TEST_DIR/validate.sh"

# Test variables used by system
export DADAD_TEST__CURRENT_SUITE="$1"
export DADAD_TEST__CURRENT_TEST="unknown"
dadad_api_set_errorexit test.path "$DADAD_TEST_DIR"
dadad_api_set_errorexit test.result.path $DADAD_TEST_RESULT_DIR
dadad_api_set_errorexit test.suite "$DADAD_TEST__CURRENT_SUITE"
dadad_api_set_errorexit test.test "$DADAD_TEST__CURRENT_TEST"

## TEST.SH HELP     ######################################################################

# param #1: subtest name
# return : DADAD_TEST__CURRENT_SUBTEST is the current subtest name
dadad_test__declaretest () {
    export DADAD_TEST__REASON=""
	export DADAD_TEST__CURRENT_TEST="$1"
	dadad_api_set_errorexit test.test $1
}

dadad_test__waitdone () {	
	TICK=1
	dadad_api_get_state_token $DADADAPI_LAST_START_NAME

	while [ "$DADADAPI_LAST_STATE_TOKEN" '!=' DEAD -a "$DADADAPI_LAST_STATE_TOKEN" '!=' FAILED ] ; do
		if [ "$TICK" -gt "$TEST_TICK_LIMIT" ] ; then
			echo "Test hanged.  Abandoning."
			return 9
		fi
	
		sleep 1
		dadad_api_get_state_token $DADADAPI_LAST_START_NAME
		TICK=$((TICK+1))
	done	
}

dadad_test__dostatus () {
	dadad_api_get_state $DADADAPI_LAST_START_NAME
	STATE=`printf "%s" "$DADADAPI_LAST_STATE" | grep "type=" | cut -d'=' -f2`
	echo "$DADAD_TEST__CURRENT_TEST:$STATE" >> $DADAD_TEST_STATUS_FILE
	echo "Test status for $DADAD_TEST__CURRENT_SUITE - $DADAD_TEST__CURRENT_TEST: $STATE"
}

dadad_test__waitandstatus () {	
	dadad_test__waitdone	
	if [ $? -ne 0 ] ; then
		echo "$DADAD_TEST__CURRENT_TEST:FAILED" >> $DADAD_TEST_STATUS_FILE
		echo "Test status for DADAD_TEST__CURRENT_SUITE - $DADAD_TEST__CURRENT_TEST: FAILED (abandoned)"
	else
		dadad_test__dostatus $DADAD_TEST__CURRENT_SUBTEST	
	fi
}

dadad_test__check_report () {	
	TICK=1
	dadad_api_get_state_token $DADADAPI_LAST_START_NAME

	while [ "$DADADAPI_LAST_STATE_TOKEN" '!=' DEAD -a "$DADADAPI_LAST_STATE_TOKEN" '!=' FAILED ] ; do
		if [ "$TICK" -gt "$TEST_TICK_LIMIT" ] ; then
			echo "Test hanged.  Abandoning."
			return 9
		fi
	
		sleep 1
		dadad_api_get_state_token $DADADAPI_LAST_START_NAME
		TICK=$((TICK+1))
	done	
}

# param #1 : test name
# param #2 : var name
# param #3 : value
dadad_test__set_test_var () {
	export "DADAD_TEST_VAR__$1$2"="$3"		
}

# param #1 : test name
# param #2 : var name
# param #3 : destination var name
dadad_test__get_test_var () {	
	___VAR_NAME="DADAD_TEST_VAR__$1$2"
	eval "___VALUE=\${$___VAR_NAME}"
	export "$3"="$___VALUE"		
}

## VALIDATE.SH HELP     ######################################################################

# param #1: subtest name
dadad_validation__declaretest () {
    if [ "${DADAD_TEST__CURRENT_TEST}" != "unknown" ] ; then
        dadad_validation_complete
    fi

    trap 'dadad_validate__fail '"$1"' ' EXIT
    export DADAD_TEST__CURRENT_TEST="$1"
}

# param #1: subtest name
# return : DADAD_TEST__CURRENT_SUBTEST is the current subtest name
dadad_validation_complete () {
	echo "$DADAD_TEST__CURRENT_TEST:PASS" >> $DADAD_TEST_VALIDATION_FILE
	echo "Test validation for test $DADAD_TEST__CURRENT_TEST: PASS"
}

# param #1: subtest name
# param #2: OPTIONAL reason.  If blank and DADAD_TEST__REASON is not blank, it will use DADAD_TEST__REASON.
# return : DADAD_TEST__CURRENT_SUBTEST is the current subtest name
dadad_validate__fail () {
    __REASON="$2"
    if [ -z "$__REASON" ] ; then
        if [ ! -z "$DADAD_TEST__REASON" ] ; then
            __REASON="$DADAD_TEST__REASON"
        fi
    fi

	echo "$DADAD_TEST__CURRENT_TEST:FAIL:$__REASON" >> $DADAD_TEST_VALIDATION_FILE
	echo "Validation for test $DADAD_TEST__CURRENT_TEST: FAIL : $__REASON"
}

# param #1: property name
# param #2: name of destination variable
# return : param 2 is value of the named property
dadad_get_prop () {
	export "$2"=`grep -i "$1=" "$DADAD_TEST_CONFIG_FILE" | cut -d'=' -f2`
}

# HAX
# param #: name of variable
dadad_fix_file () {
        eval "__VALUE=\${$1}"
        if [ "${__VALUE#file:}" != "${__VALUE}" ] ; then
                export "$1"=`echo $__VALUE | cut -c6-`
        fi
}

# param #1: file to scan
# param #2: what to find
# param #3: optional note
# return : EXIT if not found and export DADAD_TEST__REASON with the reason
dadad_scan () {
	export DADAD_TEST__REASON="Failed to find '$2' : $3"
	grep -q "$2" $1
	if [ $? -ne 0 ] ; then 
		exit 9; 
	fi				
}

# param #1: file to scan
# param #2: what to find
# param #3: optional note
# return : EXIT if it is found and export DADAD_TEST__REASON with the reason
dadad_scan_no_find () {
	export DADAD_TEST__REASON="Failed because found '$2' : $3"
	grep -q "$2" $1
	if [ $? -eq 0 ] ; then 
		exit 9; 
	fi				
}

dadad_get_report_log () {
	dadad_api_report_log_url "$1"
	_REPORT="$DADADAPI_LAST_REPORT_LOG_URL"
	dadad_fix_file "_REPORT"
	
	_REPORT=`echo $_REPORT`
	if [ -z "$_REPORT" ]; then
		echo "Report log does not exist for this test."
		exit 9	
	fi
	
	export DADAD_TEST__REPORT_LOG="$_REPORT"
}

## EXECUTION     #################################################################

test_do_exit () {
	if [ -f "$DADAD_TEST_CLEAN" ]; then
		trap "echo Test $1 failed to clean after test; test_do_exit" EXIT
		source "$DADAD_TEST_CLEAN"
	fi
}

if [ -f "$DADAD_TEST_SETUP" ]; then
	trap "echo Setup for test $1 failed; test_do_exit" EXIT
	source "$DADAD_TEST_SETUP"
fi

trap "echo Test $1 failed to run; test_do_exit; exit 9" EXIT
. "$DADAD_TEST_LIST"
dadad_api_savewpprop $DADAD_TEST_CONFIG_FILE $DADADAPI_LAST_START_NAME

if [ -f "$DADAD_TEST_VALIDATE" ]; then
	trap "echo Validation for test $1 failed; test_do_exit; echo "VALIDATION:FAILED" >> $DADAD_TEST_VALIDATION_FILE" EXIT
	export DADAD_TEST__CURRENT_TEST="unknown"
	. "$DADAD_TEST_VALIDATE"
	if [ "${DADAD_TEST__CURRENT_TEST}" != "unknown" ] ; then
        dadad_validation_complete
    fi
fi

trap "test_do_exit" EXIT
exit 0

