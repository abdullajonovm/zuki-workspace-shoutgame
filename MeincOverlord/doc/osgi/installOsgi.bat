@echo off

REM if "%1" == "" goto err
REM if "%2" == "" goto err
REM if "%3" == "" goto err
REM if "%4" == "" goto err

goto ok

:err

echo --------
echo Invalid Parameters
echo --------
goto eof

:ok

@echo .
@echo .
@echo .

REM copy JavaService.exe %3\bin\OsgiService.exe > nul

JavaService.exe -install MrSoa142 "%1\jre\bin\%4\jvm.dll" -Xbootclasspath/a: -Djava.class.path=lib\framework.jar;lib\log4j-1.2.8.jar;lib\com.springsource.org.apache.commons.logging-1.1.1.jar;lib\log4j-tools-1.0.jar;lib\meinc-launcher-1.0.jar;lib\ -Dmeinc.server.properties.file=%2 -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J -Dlogging.base=%3 -Xms64m -Xmx64m -Dmrsoa.server.port=9119 -start org.knopflerfish.framework.Main -params -xargs "%3\bin\meinc.xargs" -out %3\logs\sysout.log -err %3\logs\syserr.log -current %3 %5 -overwrite -description "Me Inc OSGI Container"

goto eof

:eof

@echo .
@echo .
@echo .
