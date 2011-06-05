set JAVA_OPTS=-Xms512m -Xmx2048m -XX:PermSize=128m -XX:MaxPermSize=256m
set MAVEN_OPTS=-Xms512m -Xmx2048m -XX:PermSize=128m -XX:MaxPermSize=256m
rem -Djetty.port=8080
mvn jetty:run -DskipTests=true -DskipITs=true