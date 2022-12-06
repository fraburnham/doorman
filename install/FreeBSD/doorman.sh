#!/bin/sh

# PROVIDE: doorman
# REQUIRE: NETWORKING DAEMON FILESYSTEMS
# KEYWORD: nojail

. /etc/rc.subr

name="doorman"
rcvar="doorman_enable"
command="/usr/home/_doorman/start.sh"
export $(JAVAVM_DRYRUN=yes /usr/local/bin/java | grep JAVAVM_PROG)
procname="$JAVAVM_PROG"

load_rc_config $name
run_rc_command "$1"
