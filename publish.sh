#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for play_version in 2.{3.9,4.0}; do
  for scala_version in 2.{10.5,11.6}; do
    if [ $play_version = "2.3.9" ]; then cd $SCRIPT_DIR/play23; else cd $SCRIPT_DIR; fi;
    PLAY_VERSION=$play_version sbt ++$scala_version publish-signed
  done
done

cd $SCRIPT_DIR
PLAY_VERSION=2.5.0 sbt ++2.11.11 publish-signed

cd $SCRIPT_DIR/play26
PLAY_VERSION=2.6.2 sbt ++2.11.11 publish-signed
PLAY_VERSION=2.6.2 sbt ++2.12.2 publish-signed
