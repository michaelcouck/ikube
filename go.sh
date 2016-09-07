#!/bin/sh

export MAVEN_OPTS="$MAVEN_OPTS -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
echo "Maven options : " $MAVEN_OPTS

# Skipping tests is optional as it takes quite long for the whole project
# -DskipTests=true -DskipITs=true

# Plain and simple run, no need to clean most of the time
# mvn install

# This command is one module at a time
# mvn install -DskipTests=true -DskipITs=true

# The command below parallelises the modules being built, and is considerably faster
mvn -T 1C install -DskipTests=true -DskipITs=true -am