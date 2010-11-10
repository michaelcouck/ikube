set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
rem dotuml:generate
mvn clean package javadoc:javadoc