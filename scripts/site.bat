cd /d ../
set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
mvn -DskipTests=true clean site