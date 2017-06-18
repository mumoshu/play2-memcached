#!/bin/bash

for play_version in 2.{3.9,4.0}; do
  for scala_version in 2.{10.5,11.6}; do
    PLAY_VERSION=$play_version sbt ++$scala_version publish-signed
  done
done

PLAY_VERSION=2.5.0 sbt ++2.11.11 publish-signed