#!/bin/bash

set -e

mvn io.quarkus:quarkus-maven-plugin:1.9.2.Final:create-extension -N \
    -DgroupId=de.fstab.demo \
    -DartifactId=superrandom-extension \
    -Dversion=1.0-SNAPSHOT \
    -Dquarkus.artifactIdBase=superrandom-extension \
    -Dquarkus.artifactIdPrefix=superrandom- \
    -Dquarkus.nameBase="Super Random"
