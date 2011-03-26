set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m

mvn -Djetty.port=80 jetty:run -DskipTests=true -DskipITs=true