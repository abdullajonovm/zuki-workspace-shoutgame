@ECHO OFF
cd ..
%JAVA_HOME%\bin\java -server -Xms64m -Xmx64m -Dmrsoa.server.port=9119 -Dmeinc.server.properties.file=C:\mrsoa142\mrsoa.properties -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J -Dlogging.base=c:\mrsoa142\www\osgi-r4 -classpath lib\framework.jar;lib\com.springsource.org.apache.commons.logging-1.1.1.jar;lib\log4j-1.2.8.jar;lib\log4j-tools-1.0.jar;lib\meinc-launcher-1.0.jar;lib\ org.knopflerfish.framework.Main -xargs bin\meinc.xargs -istart consoletty/consoletty-2.0.0.jar
cd bin

REM -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y 
REM -agentlib:yjpagent 
REM -agentpath:"C:\Program Files\NetBeans 6.0\profiler2\lib\deployed\jdk15\windows\profilerinterface.dll=\"C:\Program Files\NetBeans 6.0\profiler2\lib\"",5140 
REM -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false 
