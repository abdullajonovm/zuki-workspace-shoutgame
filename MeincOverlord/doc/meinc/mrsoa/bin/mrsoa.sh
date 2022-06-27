#/bin/sh

MEINC_HOME=/opt/meinc
MRSOA_HOME=$MEINC_HOME/mrsoa
MRSOA_PID=/var/run/mrsoa/mrsoa.pid

if [ "`id -un`" != "mrsoa" ]; then
    echo "Invalid user"
    echo "Must be mrsoa"
    exit 1
fi

if [ ! `which java` ]; then
    echo "A Java runtime must exist in the path"
    exit 1
fi

if [ ! -d "$MRSOA_HOME" ]; then
    echo "MrSOA does not exist in expected location $MRSOA_HOME"
    exit 1
fi

if [ -z "$MRSOA_OPTS" ]; then
    MRSOA_OPTS="-Xms64m -Xmx64m \
		-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n \
                -Dmrsoa.server.port=9119 \
                -Dmeinc.server.properties.file=$MEINC_HOME/meinc.properties \
                -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J \
                -Dlogging.base=$MRSOA_HOME"
fi

if [ -z "$MRSOA_CLASSPATH" ]; then
    MRSOA_CLASSPATH="lib/framework.jar:lib/com.springsource.org.apache.commons.logging-1.1.1.jar:lib/log4j-1.2.8.jar:lib/log4j-tools-1.0.jar:lib/meinc-launcher-1.0.jar:lib/"
fi

if [ -z "$MRSOA_XARGS" ]; then
    MRSOA_XARGS="bin/mrsoa.xargs"
fi

cd "$MRSOA_HOME"

case "$1" in
    start)
        shift
        if [ -f "MRSOA_PID" ]; then
            echo "MrSOA Already Running"
            exit 1
        fi
        java -server $MRSOA_OPTS \
            -classpath "$MRSOA_CLASSPATH" \
            org.knopflerfish.framework.Main \
            -xargs "$MRSOA_XARGS" \
            $@ > "$MRSOA_HOME/logs/stdout.log" 2> "$MRSOA_HOME/logs/stderr.log" &
        echo $! > "$MRSOA_PID"
        ;;
    run)
        shift
        if [ -f "MRSOA_PID" ]; then
            echo "MrSOA already running"
            exit 1
        fi
        java -server $MRSOA_OPTS \
            -classpath "$MRSOA_CLASSPATH" \
            org.knopflerfish.framework.Main \
            -xargs "$MRSOA_XARGS" \
            -istart consoletty/consoletty-2.0.0.jar \
            $@ 
        ;;
    stop)
        shift
        if [ ! -f "$MRSOA_PID" ]; then
            echo "MrSOA not running"
            exit 1
        fi
        kill `cat $MRSOA_PID`
        ;;
    forcestop)
        shift
        if [ ! -f "$MRSOA_PID" ]; then
            echo "MrSOA not running"
            exit 1
        fi
        kill -9 `cat $MRSOA_PID`
        ;;
    *)
        echo "Usage: $0 start|run|stop|forcestop"
        exit 1
        ;;
esac

exit 0

#-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y 
#-agentlib:yjpagent 
#-agentpath:"C:\Program Files\NetBeans 6.0\profiler2\lib\deployed\jdk15\windows\profilerinterface.dll=\"C:\Program Files\NetBeans 6.0\profiler2\lib\"",5140 
#-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false 
