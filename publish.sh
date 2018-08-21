#!/bin/bash

PLAY_VERSION=2.7.0-M1 sbt ++2.11.12 publish-signed
PLAY_VERSION=2.7.0-M1 sbt ++2.12.6 publish-signed
# not working yet, wait for final Play 2.7 and Scala 2.13 release:
#PLAY_VERSION=2.7.0-M1 sbt ++2.13.0-M4 publish-signed
