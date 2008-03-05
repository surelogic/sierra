#!/bin/bash
#
# Init.d file for SureLogic Sierra Service
#
# chkconfig: 2345 55 25
# description: SureLogic Sierra Service
#
# processname: sierra

# Author: support@surelogic.com
# After copying this script to /etc/init.d/sierra, execute the following command:
#
# chkconfig --add sierra

# source function library
. /etc/rc.d/init.d/functions

# Sierra Environment
export SIERRA_HOME=/home/sierra
export JETTY_HOME=$SIERRA_HOME/jetty
export JETTY_PID=/var/run/sierra.pid
export JETTY_USER=sierra
export JAVA_HOME=$SIERRA_HOME/jre/

DAEMON=$JETTY_HOME/bin/jetty.sh
DAEMON_ARGS="$JETTY_HOME/etc/sierra-embedded-derby.xml"

case "$1" in
    start)
	echo -n $"Starting Sierra Service: "
        $DAEMON start $DAEMON_ARGS >/dev/null 2>&1 && success || failure
        ;;
    stop)
	echo -n $"Stopping Sierra Service: "
        $DAEMON stop >/dev/null 2>&1 && success || failure
        ;;
    restart)
	echo -n $"Restarting Sierra Service: "
        $DAEMON restart >/dev/null 2>&1 && success || failure
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
	failure
        exit 1
        ;;
    esac

echo

exit 0
