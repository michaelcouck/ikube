#!/bin/sh
# 6096
export JAVA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m"
export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n $JAVA_OPTS"
