cd /d ../code/war
set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
rem -DskipTests=true javadoc:javadoc
mvn clean install -DskipTests=true -DskipITs=true