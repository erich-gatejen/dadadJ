dadad_get_prop	__WFSDump.url	_DUMPFILE
dadad_fix_file _DUMPFILE

#trap "dadad_validate__fail test1 $DADAD_TEST__REASON" EXIT
dadad_validation__declaretest test.text.1
dadad_scan "$_DUMPFILE" "term.text :111 Main St"
dadad_scan "$_DUMPFILE" "term.element.text :415-555-1234"
dadad_scan "$_DUMPFILE" "term.element.text :6/1/1970"
dadad_scan "$_DUMPFILE" "term.text :Jackson"

dadad_validation__declaretest check.workflow
dadad_test__get_test_var test1 wpname _WPNAME
dadad_get_report_log "$_WPNAME"
dadad_scan_no_find "$DADAD_TEST__REPORT_LOG" "FAILED" "Workflow test failed.  See logfile at $DADAD_TEST__REPORT_LOG"
