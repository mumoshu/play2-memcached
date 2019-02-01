#!/bin/bash

PLAY_VERSION=2.7.0 sbt ++2.11.12 publishSigned
PLAY_VERSION=2.7.0 sbt ++2.12.8 publishSigned
PLAY_VERSION=2.7.0 sbt ++2.13.0-M5 publishSigned
