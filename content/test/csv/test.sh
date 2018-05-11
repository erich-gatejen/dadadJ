dadad_test__declaretest test1
dadad_api_loadprop $DADAD_TEST_DIR/csv.prop
dadad_api_set_errorexit source '${{test.path}}/test.csv' test.csv
dadad_api_start dadad.system.data.DataWorkflow
dadad_test__set_test_var test1 wpname "$DADADAPI_LAST_START_NAME"
dadad_test__waitandstatus

