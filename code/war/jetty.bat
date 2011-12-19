rem -Djetty.port=8080
rem set JAVA_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
rem set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n %MAVEN_OPTS%
set MAVEN_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m

mvn jetty:run -DskipTests=true -DskipITs=true