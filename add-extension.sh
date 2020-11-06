#!/bin/bash

set -e

./mvnw quarkus:add-extension \
    -Dextensions=de.fstab.demo:superrandom-extension:1.0-SNAPSHOT
