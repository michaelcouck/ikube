#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
#export JAVA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m"
#export MAVEN_OPTS="-XX:PermSize=1024m -XX:MaxPermSize=2048m -Xms12000m -Xmx24000m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
#set MAVEN_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx2048m
#export MAVEN_OPTS=-javaagent:"/home/michael/.m2/repository/org/springframework/spring-agent/2.5.6/spring-agent-2.5.6.jar"
mvn jetty:run -DskipTests=true -DskipITs=true