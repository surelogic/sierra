#! /bin/sh
### BEGIN INIT INFO
# Provides:          sierra
# Required-Start:    $local_fs $remote_fs
# Required-Stop:     $local_fs $remote_fs
# Default-Start:     2 3 4 5
# Default-Stop:      S 0 1 6
# Short-Description: SureLogic Sierra Service
# Description:       SureLogic Sierra Team and Portal Service
#                    
### END INIT INFO

# Author: support@surelogic.com
# After copying this script to /etc/init.d/sierra, execute the following command:
#
#  update-rc.d sierra defaults

# Sierra Environment
export SIERRA_HOME=/home/sierra
export JETTY_HOME=$SIERRA_HOME/jetty
export JETTY_PID=/var/run/sierra.pid
export JETTY_USER=sierra
export JAVA_HOME=$SIERRA_HOME/jre/

# PATH should only include /usr/* if it runs after the mountnfs.sh script
PATH=/usr/sbin:/usr/bin:/sbin:/bin
DESC="SureLogic Sierra Service"
NAME=sierra
DAEMON=$JETTY_HOME/bin/jetty.sh
DAEMON_ARGS="$JETTY_HOME/etc/sierra-embedded-derby.xml"
SCRIPTNAME=/etc/init.d/$NAME

# Exit if the package is not installed
[ -x "$DAEMON" ] || exit 0

# Load the VERBOSE setting and other rcS variables
[ -f /etc/default/rcS ] && . /etc/default/rcS

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.0-6) to ensure that this file is present.
. /lib/lsb/init-functions

#
# Function that starts the daemon/service
#
do_start()
{
	RESULT=eval $DAEMON start $DAEMON_ARGS
	return $RESULT
}

#
# Function that stops the daemon/service
#
do_stop()
{
	RESULT=eval $DAEMON stop
	return $RESULT
}

#
# Function that sends a SIGHUP to the daemon/service
#
do_reload() {
	RESULT=eval $DAEMON restart
	return $RESULT
}

case "$1" in
  start)
	[ "$VERBOSE" != no ] && log_daemon_msg "Starting $DESC" "$NAME"
	do_start
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  stop)
	[ "$VERBOSE" != no ] && log_daemon_msg "Stopping $DESC" "$NAME"
	do_stop
	case "$?" in
		0|1) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  #reload|force-reload)
	#
	# If do_reload() is not implemented then leave this commented out
	# and leave 'force-reload' as an alias for 'restart'.
	#
	#log_daemon_msg "Reloading $DESC" "$NAME"
	#do_reload
	#log_end_msg $?
	#;;
  restart|force-reload)
	#
	# If the "reload" option is implemented then remove the
	# 'force-reload' alias
	#
	log_daemon_msg "Restarting $DESC" "$NAME"
	do_stop
	case "$?" in
	  0|1)
		do_start
		case "$?" in
			0) log_end_msg 0 ;;
			1) log_end_msg 1 ;; # Old process is still running
			*) log_end_msg 1 ;; # Failed to start
		esac
		;;
	  *)
	  	# Failed to stop
		log_end_msg 1
		;;
	esac
	;;
  *)
	#echo "Usage: $SCRIPTNAME {start|stop|restart|reload|force-reload}" >&2
	echo "Usage: $SCRIPTNAME {start|stop|restart|force-reload}" >&2
	exit 3
	;;
esac

:
