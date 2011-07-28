cd /d ../
set MAVEN_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx3072m
rem -DskipTests=true javadoc:javadoc
rem -DskipTests=true -DskipITs=true
mvn clean install -DskipTests=true -DskipITs=true