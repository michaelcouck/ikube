rem mvn -Dtomcat.port=80 tomcat:run
rem set JAVA_OPTS=-javaagent:serenity/serenity.jar -Dincluded.packages=com.ikokoon -Dexcluded.packages=database:persistence:model -Dincluded.adapters=profiling

set MAVEN_OPTS=-Xms256m -Xmx1024m -XX:MaxPermSize=128m
mvn tomcat:run