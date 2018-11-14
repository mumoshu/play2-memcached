#!/bin/bash

PLAY_VERSION=2.7.0-RC3 sbt ++2.11.12 publishSigned
PLAY_VERSION=2.7.0-RC3 sbt ++2.12.7 publishSigned
# not working yet, wait for final Play 2.7 and Scala 2.13 release:
#PLAY_VERSION=2.7.0-RC3 sbt ++2.13.0-M4 publishSigned
