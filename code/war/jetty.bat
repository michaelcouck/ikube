rem -Djetty.port=8080
set JAVA_OPTS=-XX:PermSize=512m -XX:MaxPermSize=1024m -Xms2048m -Xmx3072m -Dikube.configuration=/media/nas/xfs/ikube
set MAVEN_OPTS=-XX:PermSize=512m -XX:MaxPermSize=1024m -Xms2048m -Xmx3072m
set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n %MAVEN_OPTS%

mvn jetty:run -DskipTests=true -DskipITs=true