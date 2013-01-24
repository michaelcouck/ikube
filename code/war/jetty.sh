#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
#export JAVA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m -javaagent:/home/michael/.m2/repository/org/springframework/spring-agent/2.5.6/spring-agent-2.5.6.jar"
#export MAVEN_OPTS="-XX:PermSize=128m -XX:MaxPermSize=256m -Xms1024m -Xmx3076m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
#export MAVEN_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx3076m"
#set MAVEN_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx2048m
#export MAVEN_OPTS=-javaagent:"/home/michael/.m2/repository/org/springframework/spring-agent/2.5.6/spring-agent-2.5.6.jar"
mvn jetty:run -DskipTests=true -DskipITs=true