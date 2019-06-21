#!/bin/bash

PLAY_VERSION=2.7.3 sbt ++2.11.12 publishSigned sonatypeRelease
PLAY_VERSION=2.7.3 sbt ++2.12.8 publishSigned sonatypeRelease
PLAY_VERSION=2.7.3 sbt ++2.13.0 publishSigned sonatypeRelease
