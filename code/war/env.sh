#!/bin/sh
# 6096
export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n $MAVEN_OPTS"
export MAVEN_OPTS="-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx1024m $MAVEN_OPTS"