set JAVA_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
set MAVEN_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
rem -Djetty.port=8080
mvn jetty:run -DskipTests=true -DskipITs=true