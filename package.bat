set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
rem dotuml:generate javadoc:javadoc
mvn package -DskipTests=true