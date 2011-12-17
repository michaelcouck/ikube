rem -Djetty.port=8080
rem set JAVA_OPTS=-XX:PermSize=64m -XX:MaxPermSize=128m -Xms512m -Xmx2048m
set MAVEN_OPTS=-XX:PermSize=64m -XX:MaxPermSize=128m -Xms512m -Xmx2048m
set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n %MAVEN_OPTS%

mvn jetty:run -DskipTests=true -DskipITs=true