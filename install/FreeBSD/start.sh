#!/bin/sh

CMD="sh -c '/usr/local/bin/java -jar /usr/home/_doorman/doorman.jar daemon 2> /usr/home/_doorman/log 1> /usr/home/_doorman/logo &'"
su -m _doorman -c "$CMD"
