#!/bin/bash

# param #1 - run

if [ "$#" -lt 1 ]; then
    echo "FAULT[99] Must specify run."
    exit 99
fi

if [ ! -d "RESULTS/$1" ] ; then
	echo "FAULT[9] Results do not exist.  RESULTS/$1"
	exit 9
fi

DADADR_LIST=".results.list"

cd RESULTS/$1
find . -type d -not -path "." | cut -c 3- > "$DADADR_LIST"

# HUNT ERRORS
DADADR_TOTAL_FAILURES=0
DADADR_TOTAL_TESTS=0
while read TESTDIR
do
    DADADR_TOTAL_TESTS=$((DADADR_TOTAL_TESTS + 1))
	DTESTDIR="$DADAD_HOME/test/RESULTS/$1/$TESTDIR"
    grep -q "FAIL" "$DTESTDIR/STATUS.txt"
	if [ $? -eq 0 ] ; then
        DADADR_TOTAL_FAILURES=$((DADADR_TOTAL_FAILURES + 1))
	else
	    grep -q ':FAIL:' "$DTESTDIR/VALIDATION.txt"
		if [ $? -eq 0 ] ; then
            DADADR_TOTAL_FAILURES=$((DADADR_TOTAL_FAILURES + 1))
		fi
	fi
done <$DADADR_LIST

# REPORT
echo '<?xml version="1.0" encoding="UTF-8"?>'
echo '<testsuites name="ci" tests="'"$DADADR_TOTAL_TESTS"'" errors="0" failures="'"${DADADR_TOTAL_FAILURES}"'">'
echo '<testsuite name="integration" tests="'"$DADADR_TOTAL_TESTS"'" errors="0" failures="'"${DADADR_TOTAL_FAILURES}"'">'
while read TESTDIR           
do

	DTESTDIR="$DADAD_HOME/test/RESULTS/$1/$TESTDIR"	
    grep -q "FAIL" "$DTESTDIR/STATUS.txt" 
	if [ $? -eq 0 ] ; then
	    FAIL_MESSAGE=`grep 'FAIL' "$DTESTDIR/STATUS.txt" | head -n 1`
        echo '   <testcase classname="'"$TESTDIR"'" name="'"$TESTDIR"'">'
        echo "       <failure message="'"'"processing failure"'"'">Failure during processing.  ${FAIL_MESSAGE}</failure>"
        echo '   </testcase>'
								
	else

	    grep -q ':FAIL:' "$DTESTDIR/VALIDATION.txt"
		if [ $? -eq 0 ] ; then
		    FAIL_MESSAGE=`grep ':FAIL:' "$DTESTDIR/VALIDATION.txt" | head -n 1`
			echo '   <testcase classname="'"$TESTDIR"'" name="'"$TESTDIR"'">'
		    echo "       <error message="'"'"validation failure"'"'">Failure during validation.  ${FAIL_MESSAGE}</error>"
		    echo '   </testcase>'

		else
			echo '   <testcase classname="'"$TESTDIR"'" name="'"$TESTDIR"'"/>'

		fi	

	fi
        
done <$DADADR_LIST
echo '</testsuite>'
echo '</testsuites>'


