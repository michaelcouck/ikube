# Don't forget to increase the memory for Maven
# export MAVEN_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx3076m"
mvn release:prepare release:perform -Dprofile=production