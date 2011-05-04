set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
rem -Djetty.port=80
mvn jetty:run -DskipTests=true -DskipITs=true