@ECHO OFF
cd ..
%JAVA_HOME%\bin\java -server -Dcom.sun.management.jmxremote=true -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J -Dlogging.base=c:\mrsoa142\www\osgi-r4 -Xms256m -Xmx512m -Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n -classpath lib\framework.jar;lib\log4j-1.2.8.jar;lib\log4j-tools-1.0.jar;lib\ org.knopflerfish.framework.Main -xargs bin\meinc.xargs -istart consoletty/consoletty-2.0.0.jar
cd bin

