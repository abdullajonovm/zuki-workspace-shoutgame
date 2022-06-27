#!/bin/bash

# A bootstrap is sometimes necessary because Maven requires all parent POMs to exist before
# buildtime. However, some parent POMs (e.g. poms/parents/pom-service/pom.xml) can't be installed
# prior to building because they reference dependencies that won't be installed until after the
# first build. Maven's Reactor doesn't seem to be able to piece this kind of puzzle together on its
# own.

cd `dirname $0`

#JAVA_HOME=/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
#/Users/mpontius/Dev/apache-maven-3.1.1/bin/mvn -f pom.cleaninstall.xml install

set -e

mvn clean install -Dbootstrap=1
mvn clean install -Dbootstrap=2
mvn clean install -Dbootstrap=3
