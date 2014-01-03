#!/bin/sh
# This statement will find all the processes with the name specified
# and kill them with extreme prejudice, usefull for Java processes that are
# hanging and wont die quietly
ps -ef | grep $1 | awk '{print $2}' | xargs kill -9


