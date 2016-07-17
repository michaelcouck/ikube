#export JAVA_OPTS="-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m"
#export MAVEN_OPTS="-XX:PermSize=1024m -XX:MaxPermSize=2048m -Xms4096m -Xmx8192m" 
#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
export MAVEN_OPTS="-XX:PermSize=1024m -XX:MaxPermSize=4096m -Xms6144m -Xmx12288m -XX:HeapDumpPath=/tmp"
#-Djava.rmi.server.hostname=192.168.1.40 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8600 -Dcom.sun.management.jmxremote.rmi.port=8600 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
mvn jetty:run -DskipTests=true -DskipITs=true