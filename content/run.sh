#!/bin/sh

# RUN A SINGLE WORKFLOW OUTSIDE OF A RUNNING SERVER

rm -rf $DADAD_HOME/data/*

java -classpath "$DADAD_HOME/lib/dadadJ.jar" \
  dadad.system.data.boot.Bootstrap \
  config.prop \
  command=both \
  source=$PWD/$1 \
  root.path=$DADAD_HOME/data  \
  run=$2 \
  field.default.text=$3
  
