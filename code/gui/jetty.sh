#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
#export JAVA_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
#export MAVEN_OPTS="-XX:PermSize=128m -XX:MaxPermSize=256m -Xms1024m -Xmx3076m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
#export MAVEN_OPTS="-XX:PermSize=512m -XX:MaxPermSize=1024m -Xms2048m -Xmx4096m"
mvn jetty:run -DskipTests=true -DskipITs=true