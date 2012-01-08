rem -Djetty.port=8080
set JAVA_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
set MAVEN_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
rem set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n %MAVEN_OPTS%

mvn jetty:run -DskipTests=true -DskipITs=true