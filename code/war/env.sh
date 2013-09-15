# 6096
export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n $MAVEN_OPTS"
export MAVEN_OPTS="-XX:PermSize=512m -XX:MaxPermSize=1024m -Xms2048m -Xmx6096m $MAVEN_OPTS"