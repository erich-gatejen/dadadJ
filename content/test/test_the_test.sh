#!/bin/bash
cd ../.. ; rm -rf test/* ; ant test ; cd test/test ; integration_test_suite.sh RUN1

