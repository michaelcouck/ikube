cd /d ../
set MAVEN_OPTS=-XX:PermSize=64m -XX:MaxPermSize=128m -Xms512m -Xmx1536m
rem -DskipTests=true javadoc:javadoc
rem -DskipTests=true -DskipITs=true
mvn clean install -DskipTests=true -DskipITs=true