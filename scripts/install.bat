cd /d ../
set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m
rem -DskipTests=true javadoc:javadoc
rem -DskipTests=true -DskipITs=true
mvn clean install -DskipTests=true -DskipITs=true