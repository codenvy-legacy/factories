#!/bin/bash

FACTORIES_HOME=`pwd`/java
$JAVA_HOME/bin/java -jar ${FACTORIES_HOME}/target/codenvy-factories-1.0-SNAPSHOT.jar $1
