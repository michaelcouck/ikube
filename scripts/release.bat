cd /d ../
set MAVEN_OPTS=-Xms512m -Xmx1024m -XX:MaxPermSize=256m

rem To have a dry run: -DdryRun=true
rem To rollback the changes: release:rollback
rem No interation: svn --non-interactive
rem Want to resume: -Dresume=false 
rem And the passwords: -Dusername=%1 -Dpassword=%2 

mvn -DXms512m -DXmx1024m -DXX:MaxPermSize=256m release:clean release:prepare release:perform