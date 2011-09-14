rem -Djetty.port=8080
rem set JAVA_OPTS=-XX:PermSize=64m -XX:MaxPermSize=128m -Xms512m -Xmx2048m

rem set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=256m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n

rem set MAVEN_OPTS=-XX:PermSize=64m -XX:MaxPermSize=128m -Xms512m -Xmx1024m
mvn jetty:run -DskipTests=true -DskipITs=true