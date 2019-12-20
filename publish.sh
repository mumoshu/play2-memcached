#!/bin/bash

PLAY_VERSION=2.8.0 sbt ++2.12.10 publishSigned sonatypeRelease
PLAY_VERSION=2.8.0 sbt ++2.13.1 publishSigned sonatypeRelease
