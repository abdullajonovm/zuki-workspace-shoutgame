#!/bin/bash

# needed for writing files that are read by other user processes (e.g. images served by Apache)
umask 003

MEINC_HOME=/opt/meinc
MRSOA_HOME=$MEINC_HOME/mrsoa
MRSOA_JAVA="$(dirname $(readlink -f $(which java)))/mrsoa_java"
MRSOA_PID_FILE=/var/run/mrsoa/mrsoa.pid

if [ "`id -un`" != "mrsoa" ]; then
    echo "Invalid user" >&2
    echo "Must be mrsoa" >&2
    exit 1
fi

if [ ! -d "$MRSOA_HOME" ]; then
    echo "MrSOA does not exist in expected location $MRSOA_HOME" >&2
    exit 1
fi

if [ ! -x "$MRSOA_JAVA" ]; then
    echo "Java does not exist at expected location $MRSOA_JAVA" >&2
    exit 1
fi

LOG_DIR="$MRSOA_HOME/logs"

if [ "$1" = "--daemonize" ]; then
    shift
    "$0" --detach "$@" &
    exit 0
fi
if [ "$1" = "--detach" ]; then
    shift
    exec <&- >&- 2>&-
    exec </dev/null >"$LOG_DIR/stdout.log" 2>"$LOG_DIR/stderr.log"
    MRSOA_DAEMON=no
fi

MRSOA_CONF="$MRSOA_HOME/conf/mrsoa.conf"
if [ -e "$MRSOA_CONF" ]; then
    OWNER=$(stat -c '%U:%G' "$MRSOA_CONF")
    PERMS=$(stat -c '%a' "$MRSOA_CONF")
    if [ "$OWNER" != "mrsoa:mrsoa" -o "$PERMS" != "600" ]; then
        echo "Found mrsoa.conf with invalid owner ($OWNER) and/or permissions ($PERMS)" >&2
        exit 1
    fi
    source "$MRSOA_CONF"
fi

if [ ! "$MRSOA_MEM_MIN" ]; then
    MRSOA_MEM_MIN=500
    if [ "$NOMAD_MEMORY_LIMIT" ]; then
        MRSOA_MEM_MIN=$[NOMAD_MEMORY_LIMIT / 2]
    fi
    MRSOA_MEM_MIN="${MRSOA_MEM_MIN}m"
fi
if [ ! "$MRSOA_MEM_MAX" ]; then
    MRSOA_MEM_MAX=1000
    if [ "$NOMAD_MEMORY_LIMIT" ]; then
        MRSOA_MEM_MAX=$[NOMAD_MEMORY_LIMIT]
    fi
    MRSOA_MEM_MAX="${MRSOA_MEM_MAX}m"
fi

if [ "$JAVA_DEBUG_PORT" ]; then
    JAVA_DEBUG_SUSPEND_ARG=y
    if [ "$JAVA_DEBUG_SUSPEND" = "false" ]; then
        JAVA_DEBUG_SUSPEND_ARG=n
    fi
    JAVA_DEBUG_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,address=$JAVA_DEBUG_PORT,server=y,suspend=$JAVA_DEBUG_SUSPEND_ARG"
fi

if [ "$YOURKIT_HOME" -a "$YOURKIT_PORT" ]; then
    YOURKIT_PROFILE_ARGS="-agentpath:$YOURKIT_HOME/bin/linux-x86-64/libyjpagent.so=listen=0.0.0.0:$YOURKIT_PORT"
fi

if [ ! "$MRSOA_OPTS" ]; then
    MRSOA_OPTS="-Xms$MRSOA_MEM_MIN -Xmx$MRSOA_MEM_MAX
                -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m
                -Xss512k
                $JAVA_DEBUG_ARGS
                $YOURKIT_PROFILE_ARGS
                -XX:NativeMemoryTracking=summary
                -XX:+HeapDumpOnOutOfMemoryError
                -Dfile.encoding=UTF-8
                -Djava.net.preferIPv4Stack=true
                -Dmrsoa.server.port=9119
                -Dmeinc.server.properties.file=$MRSOA_HOME/conf/meinc.properties
                -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J
                -Dmrsoa.home=$MRSOA_HOME
                -Dlog.dir=$LOG_DIR
                -Dwebapps.path=$MRSOA_HOME/webapps"
fi

if [ ! "$MRSOA_CLASSPATH" ]; then
    #MRSOA_CLASSPATH="lib/framework.jar:lib/com.springsource.org.apache.commons.logging-1.1.1.jar:lib/slf4j-api-1.6.4.jar:lib/slf4j-log4j12-1.6.4.jar:lib/log4j-1.2.8.jar:lib/log4j-tools-1.0.jar:lib/meinc-launcher-1.1.jar:lib/"
    MRSOA_CLASSPATH="lib/framework.jar:lib/com.springsource.org.apache.commons.logging-1.1.1.jar:lib/slf4j-api-1.6.4.jar:lib/slf4j-log4j12-1.6.4.jar:lib/jul-to-slf4j-1.7.25.jar:lib/log4j-1.2.17.jar:lib/apache-log4j-extras-1.2.17.jar:lib/log4j-tools-1.0.jar:lib/meinc-launcher-1.1.jar:lib/:conf/log4j.xml"
fi

if [ ! "$MRSOA_XARGS" ]; then
    MRSOA_XARGS="bin/mrsoa.xargs"
fi

cd "$MRSOA_HOME"

inject_xargs() {
    PREFIX='###<USER-BUNDLES>###'
    SUFFIX='###<\/USER-BUNDLES>###'
    sed -i'' -e "/$PREFIX/,/$SUFFIX/ {
        /$PREFIX/ {
            p  
            r conf/mrsoa.xargs
        }                   
        /$SUFFIX/p
        d         
    }" bin/mrsoa.xargs
}

MRSOA_CMD="$MRSOA_JAVA -server $MRSOA_OPTS
           -classpath $MRSOA_CLASSPATH
           org.knopflerfish.framework.Main
           -xargs $MRSOA_XARGS"

if [ -e /usr/bin/authbind ]; then
   MRSOA_CMD="/usr/bin/authbind --deep bash -c $MRSOA_CMD"
fi

# Check for invalid pid file
if [ -e "$MRSOA_PID_FILE" ]; then
    OLD_PID=$(cat "$MRSOA_PID_FILE")
    OLD_EXE="/proc/$OLD_PID/exe"
    if [ ! -e "$OLD_EXE" ]; then
        rm -f "$MRSOA_PID_FILE"
    else
        OLD_EXE=$(readlink -f "$OLD_EXE")
        if [ "$OLD_EXE" != "$MRSOA_JAVA" ]; then
            rm -f "$MRSOA_PID_FILE"
        fi
    fi
fi

case "$1" in
    start)
        if [ -f "$MRSOA_PID_FILE" ]; then
            echo "MrSOA Already Running" >&2
            exit 1
        fi
        if [ "$MRSOA_DAEMON" = "no" ]; then
            shift
            inject_xargs
            echo $$ >"$MRSOA_PID_FILE"
            exec $MRSOA_CMD >"$LOG_DIR/stdout.log" 2>"$LOG_DIR/stderr.log"
        else
            setsid "$0" --daemonize "$@" &
            exit 0
        fi
        ;;
    run)
        if [ -f "$MRSOA_PID_FILE" ]; then
            echo "MrSOA already running" >&2
            exit 1
        fi
        shift
        inject_xargs
        MRSOA_CMD+=" -istart consoletty/consoletty-2.0.0.jar"
        exec $MRSOA_CMD
        ;;
    stop)
        if [ ! -f "$MRSOA_PID_FILE" ]; then
            echo "MrSOA not running" >&2
            exit 1
        fi
        kill -ALRM `cat $MRSOA_PID_FILE`
        #TODO wait for comfirmed kill
        ;;
    force-stop)
        if [ ! -f "$MRSOA_PID_FILE" ]; then
            echo "MrSOA not running" >&2
            exit 1
        fi
        kill -9 `cat $MRSOA_PID_FILE`
        #TODO wait for comfirmed kill
        ;;
    *)
        echo "Usage: $0 start|run|stop|forcestop" >&2
        exit 1
        ;;
esac

exit 0
