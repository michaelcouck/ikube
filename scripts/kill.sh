#!/bin/sh
# This statement will find all the processes with the name specified
# and kill them with extreme prejudice, usefull for Java processes that are
# hanging and won't die quietly, will not go int othe night without a fight
ps -ef | grep $1 | awk '{print $2}' | xargs kill -9
# or even better
pkill -9 -f tomcat
# kills all the processes that match tomcat


