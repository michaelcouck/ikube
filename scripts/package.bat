cd /d ../
set MAVEN_OPTS=-Xms128m -Xmx512m -XX:MaxPermSize=64m
rem dotuml:generate javadoc:javadoc
mvn package -DskipTests=true -DskipITs=true