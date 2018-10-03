#!/bin/bash

PLAY_VERSION=2.7.0-M3 sbt ++2.12.14 publish-signed
# not working yet, wait for final Play 2.7 and Scala 2.13 release:
#PLAY_VERSION=2.7.0-M3 sbt ++2.13.0-M3 publish-signed
