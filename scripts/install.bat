cd /d ../
set MAVEN_OPTS=-Xms1024m -Xmx2048m -XX:PermSize=128m -XX:MaxPermSize=256m
rem -DskipTests=true javadoc:javadoc
rem  -DskipTests=true -DskipITs=true
mvn clean install