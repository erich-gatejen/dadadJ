#!/bin/sh

# Useful for sourcing into other scripts

export DADAD_DATE=$(date +%F_%H%M)
export DADAD_TEMP_DIR=$DADAD_HOME/temp
export DADAD_CURRENT_LOGFILE_FILE="$DADAD_HOME/temp/CURRENT_SERVER_LOGFILE"

export DADAD_REST_PORT=`grep "system.rest.port=" "$DADAD_HOME/server.prop" | cut -d'=' -f2`




