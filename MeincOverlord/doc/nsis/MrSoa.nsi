; This script expects most of the source files that will be bundled into the
; installer to be located in a centralized directory (C:\installer-source by
; default).  The variables below configure the specific path for the centralized
; directory and the specific subdirectories beneath it.

;Change the following variables to customize the installer
!define VERSION        "1.4.2"
!define SOURCE_DIR     "C:\installer-source"
!define JDK_DIR        "dev\jdk1.6.0_01"
!define MYSQL_DIR      "dev\mysql5027"
!define TOMCAT_DIR     "www\tomcat6010"
!define OSGI_DIR       "www\osgi-r4"
!define APACHE_DIR     "www\apache224"
!define APACHE_VC      "vcredist_x86-sp1.exe"

!define SERVICE_PREFIX "MrSoa142"
!define APACHE_SERVICE "Apache224"
!define TOMCAT_SERVICE "Tomcat6010"
!define MYSQL_SERVICE  "MySQL5027"

!define ECLIPSE_WORKSPACE_DIR "C:\workspaces\mr-soa"

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"
  !include "LogicLib.nsh"
  !include "WordFunc.nsh"
  !include "WriteEnvStr.nsh"

  !insertmacro WordReplace

;--------------------------------
;General

  ;Name and file
  Name "Mr SOA ${VERSION}"
  OutFile "MrSOA-${VERSION}-setup.exe"

  ;Default installation folder
  InstallDir "C:\"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\MrSOA" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  Page custom PageInstallPassword ValidateInstallPassword
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  Page custom PageApacheSettings
  Page custom PageTomcatSettings
  Page custom PageMySQLSettings
  Page custom PageMrSoaContainerSettings
  Page custom PageMrSoaSettings
  Page custom PageMrSoaSettings2 StopMySqlMessage
  !insertmacro MUI_PAGE_INSTFILES
  
;  !insertmacro MUI_UNPAGE_CONFIRM
;  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Reserve Files
  
  ;If you are using solid compression, files that are required before
  ;the actual installation should be stored first in the data block,
  ;because this will make your installer start faster.
  
  ReserveFile "PageInstallPassword.ini"
  ReserveFile "PageApacheSettings.ini"
  ReserveFile "PageTomcatSettings.ini"
  ReserveFile "PageMySQLSettings.ini"
  ReserveFile "PageMrSoaContainerSettings.ini"
  ReserveFile "PageMrSoaSettings.ini"
  ReserveFile "PageMrSoaSettings2.ini"
  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

;--------------------------------
;Custom Functions

Function FuncReplaceJarFileString
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1

  SetOutPath $TEMP
  nsExec::ExecToLog `"$R1\bin\jar" xvf "$R2" "$R3"`

  ;Read in target file to a temp file while replacing relevant variables, then
  ;replace the original file with the temp file
  ClearErrors
  FileOpen $R7 "$TEMP\$R3" r
  GetTempFileName $R0
  FileOpen $R8 $R0 w
  jarstring_read:
    FileRead $R7 $R9
    IfErrors jarstring_read_done
    ${WordReplace} $R9 $R4 $R5 "+" $R9
    FileWrite $R8 $R9
    Goto jarstring_read
  jarstring_read_done:
    FileClose $R7
    FileClose $R8
    Delete "$TEMP\$R3"
    CopyFiles /SILENT $R0 "$TEMP\$R3"
    Delete $R0
    ClearErrors

  nsExec::ExecToLog `"$R1\bin\jar" uf "$R2" "$R3"`

  Push 0
FunctionEnd

!macro MacroReplaceJarFileString _JDKPATH _JARFILE _FILEPATH _TARGETSTRING _REPLACESTRING _RESULT
  Push `${_JDKPATH}`
  Push `${_JARFILE}`
  Push `${_FILEPATH}`
  Push `${_TARGETSTRING}`
  Push `${_REPLACESTRING}`
  Call FuncReplaceJarFileString
  Pop ${_RESULT}
!macroend

!define ReplaceJarFileString `!insertmacro MacroReplaceJarFileString`

;--------------------------------
;Installer Sections

Section "Java 1.6.0_01 JDK" SecJavaJDK
  Var /GLOBAL JDKPATH
  StrCpy $JDKPATH "$INSTDIR\${JDK_DIR}"

  ReadEnvStr $0 JAVA_HOME
  ${If} $0 != ""
  ${AndIf} $0 != $JDKPATH
    MessageBox MB_YESNO \
      "The JAVA_HOME environment variable is currently set to $0.$\nDo you wish to replace JAVA_HOME with the new JDK location $JDKPATH?" \
      IDYES java_yes IDNO java_no
    java_yes:
      Push JAVA_HOME
      Push $JDKPATH
      Call WriteEnvStr
      Goto java_continue
    java_no:
      Goto java_continue
  ${ElseIf} $0 == ""
      Push JAVA_HOME
      Push $JDKPATH
      Call WriteEnvStr
      Goto java_continue
  ${EndIf}

  java_continue:
    SetOutPath $JDKPATH
    File /a /r "${SOURCE_DIR}\${JDK_DIR}\*.*"

  IfFileExists "$SYSDIR\msvcr71.dll" +2 +1
  CopyFiles /SILENT "$JDKPATH\jre\bin\msvcr71.dll" "$SYSDIR"
SectionEnd

Section "Apache Web Server" SecApache
  Var /GLOBAL WIN_SERVICE_NAME
  !insertmacro MUI_INSTALLOPTIONS_READ $WIN_SERVICE_NAME "PageApacheSettings.ini" "Field 2" "State"
  Var /GLOBAL SERVER_NAME
  !insertmacro MUI_INSTALLOPTIONS_READ $SERVER_NAME "PageApacheSettings.ini" "Field 4" "State"

  ;Install the Visual Studio 2005 SP1 redistributable dependencies required by
  ;this build of Apache
  SetOutPath $TEMP
  File /a "${SOURCE_DIR}\${APACHE_VC}"
  nsExec::ExecToLog '"$TEMP\${APACHE_VC}"'

  CreateDirectory "$INSTDIR\www\$SERVER_NAME"

  Var /GLOBAL APACHEPATH
  StrCpy $APACHEPATH "$INSTDIR\${APACHE_DIR}"

  SetOutPath "$APACHEPATH"
  
  File /a /r /x logs /x conf "${SOURCE_DIR}\${APACHE_DIR}\*.*"

  CreateDirectory "$APACHEPATH\logs"
  CreateDirectory "$APACHEPATH\conf"

  SetOutPath "$APACHEPATH\conf"

  File /r /x ssl.key /x ssl.crt "${SOURCE_DIR}\${APACHE_DIR}\conf\*.*"

  CreateDirectory "$APACHEPATH\conf\ssl.key"
  CreateDirectory "$APACHEPATH\conf\ssl.crt"

  Var /GLOBAL APACHECONFFILE
  StrCpy $APACHECONFFILE "$APACHEPATH\conf\httpd.conf"

  ${WordReplace} $APACHEPATH "\" "/" "+" $3
  ${WordReplace} "$INSTDIR/www" "\" "/" "+" $4

  ;Read in httpd.conf to a temp file while replacing relevant variables, then
  ;replace the original file with the temp file
  ClearErrors
  FileOpen $0 $APACHECONFFILE r
  GetTempFileName $R0
  FileOpen $1 $R0 w
  hconf_read:
    FileRead $0 $2
    IfErrors hconf_read_done
    ${WordReplace} $2 "%%APACHEPATH" $3 "+" $2
    ${WordReplace} $2 "%%WWWPATH" $4 "+" $2
    ${WordReplace} $2 "%%SERVER_NAME" $SERVER_NAME "+" $2
    FileWrite $1 $2
    Goto hconf_read
  hconf_read_done:
    FileClose $0
    FileClose $1
    Delete $APACHECONFFILE
    CopyFiles /SILENT $R0 $APACHECONFFILE
    Delete $R0
    ClearErrors
  
  Var /GLOBAL OPENSSLFILE
  StrCpy $OPENSSLFILE "$APACHEPATH\conf\openssl.cfg"

  ;Read in openssl.cfg to a temp file while replacing relevant variables, then
  ;replace the original file with the temp file
  ClearErrors
  FileOpen $0 $OPENSSLFILE r
  GetTempFileName $R0
  FileOpen $1 $R0 w
  ssl_read:
    FileRead $0 $2
    IfErrors ssl_read_done
    ${WordReplace} $2 "%%COMMON_NAME" $SERVER_NAME "+" $2
    FileWrite $1 $2
    Goto ssl_read
  ssl_read_done:
    FileClose $0
    FileClose $1
    Delete $OPENSSLFILE
    CopyFiles /SILENT $R0 $OPENSSLFILE
    Delete $R0
    ClearErrors
  
  ;Create SSL Cert
  nsExec::ExecToLog '"..\bin\openssl" genrsa -out ssl.key\server.key 2048'
  nsExec::ExecToLog '"..\bin\openssl" req -config openssl.cfg -new -x509 -key ssl.key\server.key -out ssl.crt\server.crt'

  ;Install Apache Windows Service
  SetOutPath "$APACHEPATH\bin"
  nsExec::ExecToLog 'httpd -k "install" -n "$WIN_SERVICE_NAME"'

  ;Create Apache Monitor shortcut in Startup Folder
  CreateShortcut "$SMSTARTUP\ApacheMonitor.lnk" "$APACHEPATH\bin\ApacheMonitor.exe"
  ExecShell "" "$SMSTARTUP\ApacheMonitor.lnk"
SectionEnd

Section "MySQL Database" SecMySQL
  Var /GLOBAL MYSQLPATH
  StrCpy $MYSQLPATH "$INSTDIR\${MYSQL_DIR}"

  SetOutPath "$MYSQLPATH"
  File /a /r "${SOURCE_DIR}\${MYSQL_DIR}\*.*"

  Var /GLOBAL MYSQLINIFILE
  StrCpy $MYSQLINIFILE "$MYSQLPATH\my.ini"

  ;Read in my.ini to a temp file while replacing relevant variables,
  ;then replace the original file with the temp file
  ${WordReplace} $MYSQLPATH "\" "/" "+" $3
  ClearErrors
  FileOpen $0 $MYSQLINIFILE r
  GetTempFileName $R0
  FileOpen $1 $R0 w
  hconf_read:
    FileRead $0 $2
    IfErrors hconf_read_done
    ${WordReplace} $2 "%%MYSQLPATH" $3 "+" $2
    FileWrite $1 $2
    Goto hconf_read
  hconf_read_done:
    FileClose $0
    FileClose $1
    Delete $MYSQLINIFILE
    CopyFiles /SILENT $R0 $MYSQLINIFILE
    Delete $R0
    ClearErrors

  Var /GLOBAL MYSQL_SERVICE_NAME
  !insertmacro MUI_INSTALLOPTIONS_READ $MYSQL_SERVICE_NAME "PageMySQLSettings.ini" "Field 2" "State"
  ${WordReplace} $MYSQL_SERVICE_NAME " " "" "+" $MYSQL_SERVICE_NAME

  SetOutPath "$MYSQLPATH\bin"
  nsExec::ExecToLog '.\mysqld-nt --install $MYSQL_SERVICE_NAME --defaults-file="$MYSQLPATH\my.ini"'

  Var /GLOBAL MYSQL_ROOT_PASS
  !insertmacro MUI_INSTALLOPTIONS_READ $MYSQL_ROOT_PASS "PageMySQLSettings.ini" "Field 4" "State"

  ${If} $MYSQL_ROOT_PASS != ""
    DetailPrint "Starting MySQL"
    nsExec::ExecToLog 'NET START "$MYSQL_SERVICE_NAME"'
    Sleep 4000
    DetailPrint "Setting MySQL Root Password"
    StrCpy $1 0
    mysql_set_pass:
    nsExec::ExecToLog `.\mysql -u root -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('$MYSQL_ROOT_PASS')"`
    Pop $0
    IntOp $1 $1 + 1
    ${If} $0 != 0
    ${AndIf} $1 < 5
      DetailPrint "Retrying"
      Sleep 4000
      Goto mysql_set_pass
    ${ElseIf} $0 = 0
    ${AndIf} $1 >= 5
      DetailPrint "Giving Up"
      Var /GLOBAL MYSQL_ROOT_PASS_FAILED
      StrCpy $MYSQL_ROOT_PASS_FAILED 1
    ${EndIf}
    DetailPrint "Stopping MySQL"
    Sleep 1000
    nsExec::ExecToLog 'NET STOP "$MYSQL_SERVICE_NAME"'
  ${EndIf}
SectionEnd

Section "Tomcat Servlet Container" SecTomcat
  Var /GLOBAL TOMCATPATH
  StrCpy $TOMCATPATH "$INSTDIR\${TOMCAT_DIR}"

  SetOutPath "$TOMCATPATH"
  File /a /r "${SOURCE_DIR}\${TOMCAT_DIR}\bin"
  File /a /r "${SOURCE_DIR}\${TOMCAT_DIR}\conf"

  CreateDirectory "$TOMCATPATH\logs"
  CreateDirectory "$TOMCATPATH\meinc"
  CreateDirectory "$TOMCATPATH\temp"
  CreateDirectory "$TOMCATPATH\work"

  SetOutPath "$TOMCATPATH\webapps"
  File /a /r "${SOURCE_DIR}\${TOMCAT_DIR}\webapps\manager"
  File /a /r "${SOURCE_DIR}\${TOMCAT_DIR}\webapps\host-manager"

  SetOutPath "$TOMCATPATH\lib"
  File /r /x activemq-*.jar /x commons-pool-*.jar /x osgi-*.jar "${SOURCE_DIR}\${TOMCAT_DIR}\lib\*.*"

  Var /GLOBAL TOMCATUSERSFILE
  StrCpy $TOMCATUSERSFILE "$TOMCATPATH\conf\tomcat-users.xml"

  Var /GLOBAL MANAGER_PASS
  !insertmacro MUI_INSTALLOPTIONS_READ $MANAGER_PASS "PageTomcatSettings.ini" "Field 2" "State"
  ${WordReplace} $MANAGER_PASS " " "" "+" $MANAGER_PASS

  ;Read in tomcat-users.xml to a temp file while replacing relevant variables,
  ;then replace the original file with the temp file
  ClearErrors
  FileOpen $0 $TOMCATUSERSFILE r
  GetTempFileName $R0
  FileOpen $1 $R0 w
  hconf_read:
    FileRead $0 $2
    IfErrors hconf_read_done
    ${WordReplace} $2 "%%MANAGER_PASS" $MANAGER_PASS "+" $2
    FileWrite $1 $2
    Goto hconf_read
  hconf_read_done:
    FileClose $0
    FileClose $1
    Delete $TOMCATUSERSFILE
    CopyFiles /SILENT $R0 $TOMCATUSERSFILE
    Delete $R0
    ClearErrors

  Var /GLOBAL TOMCAT_SERVICE_NAME
  !insertmacro MUI_INSTALLOPTIONS_READ $TOMCAT_SERVICE_NAME "PageTomcatSettings.ini" "Field 2" "State"
  ${WordReplace} $TOMCAT_SERVICE_NAME " " "" "+" $TOMCAT_SERVICE_NAME
  DetailPrint "Using Tomcat Service Name '$TOMCAT_SERVICE_NAME'"

  ${If} $JDKPATH == ""
    ReadEnvStr $JDKPATH JAVA_HOME
  ${EndIf}

  ${If} $JDKPATH == ""
    MessageBox MB_ICONEXCLAMATION|MB_OK "The JAVA_HOME environment variable is not set.$\nThe Tomcat Windows Service cannot not be installed."
  ${Else}
    SetOutPath "$TOMCATPATH"
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("CATALINA_HOME", "$TOMCATPATH").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("CATALINA_BASE", "$TOMCATPATH").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_DESCRIPTION", "Apache Tomcat Server").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_INSTALL", "$TOMCATPATH\bin\tomcat6.exe").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_LOGPATH", "$TOMCATPATH\logs").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_CLASSPATH", "$TOMCATPATH\bin\bootstrap.jar").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_JVM", "$JDKPATH\jre\bin\server\jvm.dll").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_STDOUTPUT", "auto").r0'
    System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PR_STDERROR", "auto").r0'
    nsExec::ExecToLog '"$TOMCATPATH\bin\tomcat6.exe" //IS//$TOMCAT_SERVICE_NAME --Startup auto --JvmOptions -Dcatalina.base=$TOMCATPATH#-Dcatalina.home=$TOMCATPATH#-Djava.endorsed.dirs=$TOMCATPATH\endorsed#-Djava.io.tmpdir=$TOMCATPATH\temp#-Dorg.apache.juli.logging.impl.LogFactoryImpl=org.apache.juli.logging.impl.Log4JLogger#-Dlogging.base=$TOMCATPATH --JvmMs 256 --JvmMx 512 --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartMode jvm --StopMode jvm --StartParams start --StopParams stop'

    CreateShortcut "$SMSTARTUP\TomcatMonitor_$TOMCAT_SERVICE_NAME.lnk" "$TOMCATPATH\bin\tomcat6w.exe" "//MS//$TOMCAT_SERVICE_NAME"
    SetOutPath "$SMSTARTUP"
    ExecShell "" "TomcatMonitor_$TOMCAT_SERVICE_NAME.lnk"
  ${EndIf}
SectionEnd

Section "MrSOA Container" SecMrSoaContainer
  Var /GLOBAL OSGIPATH
  StrCpy $OSGIPATH "$INSTDIR\${OSGI_DIR}"

  SetOutPath $OSGIPATH
  File /a /r "${SOURCE_DIR}\${OSGI_DIR}\bin"
  File /a /r "${SOURCE_DIR}\${OSGI_DIR}\lib"
  File /a /r "${SOURCE_DIR}\${OSGI_DIR}\bundles"
  File /a /r "${SOURCE_DIR}\${OSGI_DIR}\framework"

  CreateDirectory "$OSGIPATH\logs"
  CreateDirectory "$OSGIPATH\deploy"

  Var /GLOBAL MS_SERVICE_NAME
  !insertmacro MUI_INSTALLOPTIONS_READ $MS_SERVICE_NAME "PageMrSoaSettings.ini" "Field 2" "State"
  ${WordReplace} $MS_SERVICE_NAME " " "" "+" $MS_SERVICE_NAME
  ${If} $MS_SERVICE_NAME == ""
    StrCpy $MS_SERVICE_NAME ${SERVICE_PREFIX}
  ${EndIf}

  ${If} $JDKPATH == ""
    ReadEnvStr $0 JAVA_HOME
  ${Else}
    StrCpy $0 $JDKPATH
  ${EndIf}

  SetOutPath "$OSGIPATH\bin\Windows Service"
  nsExec::ExecToLog 'JavaService.exe -install $MS_SERVICE_NAME "$0\jre\bin\server\jvm.dll" -Xbootclasspath/a: -Djava.class.path=lib\log4j-1.2.8.jar;lib\log4j-tools-1.0.jar;lib\framework.jar;lib\ -Dcom.sun.management.jmxremote=true -Dlog4j.configuratorClass=com.sco.log4j.tools.configurator.dom.WatchfulLog4J -Dlogging.base=$OSGIPATH -Xms256m -Xmx512m -start org.knopflerfish.framework.Main -params -xargs "$OSGIPATH\bin\meinc.xargs" -out "$OSGIPATH\logs\sysout.log" -err "$OSGIPATH\logs\syserr.log" -current "$OSGIPATH" -auto -overwrite -description "MrSOA OSGI-JMS Container"'
SectionEnd

Section "Configure MrSOA" SecMrSoaServices
  SectionGetFlags ${SecMrSoaContainer} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $9 != 0
    ;Unpack JARs/WARs
    SetOutPath "$OSGIPATH\deploy"
    File /nonfatal /a "${SOURCE_DIR}\${OSGI_DIR}\deploy\*.jar"
  ${EndIf}

  SectionGetFlags ${SecTomcat} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $9 != 0
    SetOutPath "$TOMCATPATH\meinc"
    File /nonfatal /a "${SOURCE_DIR}\${TOMCAT_DIR}\meinc\*.war"

    SetOutPath "$TOMCATPATH\webapps"
    File /nonfatal /a "${SOURCE_DIR}\${TOMCAT_DIR}\webapps\*.war"

    SetOutPath "$TOMCATPATH\lib"
    File /nonfatal /a "${SOURCE_DIR}\${TOMCAT_DIR}\lib\activemq-*.jar"
    File /nonfatal /a "${SOURCE_DIR}\${TOMCAT_DIR}\lib\commons-pool-*.jar"
    File /nonfatal /a "${SOURCE_DIR}\${TOMCAT_DIR}\lib\osgi-*.jar"
  ${EndIf}

  ${If} $JDKPATH == ""
    ReadEnvStr $0 JAVA_HOME
  ${Else}
    StrCpy $0 $JDKPATH
  ${EndIf}

  ;** For the following sections of replacement operations to work, the
  ;** PathSwitcher tool must be the last "Switcher" tool executed on the source
  ;** code before the resultant JAR/WAR files are copied into the source
  ;** directory of this installer script

  SectionGetFlags ${SecMrSoaContainer} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $9 != 0
    ;Replace in-jar file path strings
    ${WordReplace} $INSTDIR "\" "/" "+" $1
    IfFileExists "$OSGIPATH\deploy\shout2-application-2.0.jar" +1 +2
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout2-application-2.0.jar" "shout2@applicationContext.xml" "c:/" "$1/" $2
    IfFileExists "$OSGIPATH\deploy\shout-application-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout-application-2.0.jar" "shout@applicationContext.xml" "c:/" "$1/" $2
    ;${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout-application-2.0.jar" "shout@applicationContext.xml" "super.sco.com" "$SERVER_NAME" $2
    IfFileExists "$OSGIPATH\deploy\account-service-3.0.jar" +1 +2
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\account-service-3.0.jar" "account@jdbc.properties" "c:/" "$1/" $2
    IfFileExists "$OSGIPATH\deploy\adService-3.0.jar" +1 +2
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\adService-3.0.jar" "adservice@applicationContext.xml" "c:/" "$1/" $2

    ;Replace in-jar file 'sconow' user JDBC strings
    !insertmacro MUI_INSTALLOPTIONS_READ $1 "PageMrSoaSettings2.ini" "Field 2" "State"
    !insertmacro MUI_INSTALLOPTIONS_READ $2 "PageMrSoaSettings2.ini" "Field 4" "State"
    IfFileExists "$OSGIPATH\deploy\aggregation-team-service-3.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\aggregation-team-service-3.jar" "aggregation@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\aggregation-team-service-3.jar" "aggregation@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\sco-team-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\sco-team-service-3.0.jar" "scoteam@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\sco-team-service-3.0.jar" "scoteam@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\team-dispatcher-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\team-dispatcher-service-3.0.jar" "team@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\team-dispatcher-service-3.0.jar" "team@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\account-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\account-service-3.0.jar" "account@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\account-service-3.0.jar" "account@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\application-client-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\application-client-service-3.0.jar" "appclient@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\application-client-service-3.0.jar" "appclient@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\authorization-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\authorization-service-3.0.jar" "auth@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\authorization-service-3.0.jar" "auth@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\encryption-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\encryption-service-3.0.jar" "encryption@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\encryption-service-3.0.jar" "encryption@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\hosted-team-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\hosted-team-service-3.0.jar" "hostedteam@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\hosted-team-service-3.0.jar" "hostedteam@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\phoenix-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\phoenix-service-3.0.jar" "phoenix@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\phoenix-service-3.0.jar" "phoenix@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\dynamicBranding-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\dynamicBranding-3.0.jar" "dynamicBranding@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\dynamicBranding-3.0.jar" "dynamicBranding@jdbc.properties" "sconowsc0n0w" $2 $3
    ;IfFileExists "$OSGIPATH\deploy\opus-application-3.0.jar" +1 +3
    ;${ReplaceJarFileString} $0 "$OSGIPATH\deploy\opus-application-3.0.jar" "opus@jdbc.properties" "localhost\:3306" $1 $3
    ;${ReplaceJarFileString} $0 "$OSGIPATH\deploy\opus-application-3.0.jar" "opus@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\shout-application-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout-application-2.0.jar" "shout@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout-application-2.0.jar" "shout@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\postoffice-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\postoffice-service-3.0.jar" "postoffice@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\postoffice-service-3.0.jar" "postoffice@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\myco-application-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\myco-application-2.0.jar" "myco@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\myco-application-2.0.jar" "myco@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\adService-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\adService-3.0.jar" "adservice@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\adService-3.0.jar" "adservice@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\mintService-2.1.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\mintService-2.1.jar" "mintservice@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\mintService-2.1.jar" "mintservice@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\shout2-application-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout2-application-2.0.jar" "shout2@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\shout2-application-2.0.jar" "shout2@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\phoenix-billing-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\phoenix-billing-2.0.jar" "phoenixbilling@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\phoenix-billing-2.0.jar" "phoenixbilling@jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$OSGIPATH\deploy\vote2Service-2.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\vote2Service-2.0.jar" "vote2@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\vote2Service-2.0.jar" "vote2@jdbc.properties" "sconowsc0n0w" $2 $3

    ;Replace in-jar file 'billing' user JDBC strings
    !insertmacro MUI_INSTALLOPTIONS_READ $1 "PageMrSoaSettings2.ini" "Field 6" "State"
    !insertmacro MUI_INSTALLOPTIONS_READ $2 "PageMrSoaSettings2.ini" "Field 8" "State"
    IfFileExists "$OSGIPATH\deploy\billing-service-3.0.jar" +1 +3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\billing-service-3.0.jar" "billing@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\billing-service-3.0.jar" "billing@jdbc.properties" "income1nc0m3" $2 $3
    IfFileExists "$OSGIPATH\deploy\phoenix-billing-2.0.jar" +1 +2
    ${ReplaceJarFileString} $0 "$OSGIPATH\deploy\phoenix-billing-2.0.jar" "phoenixbilling@jdbc.properties" "income1nc0m3" $2 $3
  ${EndIf}

  SectionGetFlags ${SecTomcat} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $9 != 0
    ;Replace in-war file path strings
    ${WordReplace} $INSTDIR "\" "/" "+" $1
    IfFileExists "$TOMCATPATH\webapps\postcard.war" +1 +2
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\postcard.war" "WEB-INF\classes\shout2web@page.properties" "c:/" "$1/" $2
    IfFileExists "$TOMCATPATH\meinc\shout.war" +1 +2
    ${ReplaceJarFileString} $0 "$TOMCATPATH\meinc\shout.war" "WEB-INF\properties\download.properties" "c:/" "$1/" $2
    IfFileExists "$TOMCATPATH\webapps\eps.war" +1 +2
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\eps.war" "WEB-INF\classes\eps.properties" "c:/" "$1/" $2

    ;Replace in-war file 'sconow' user JDBC strings
    !insertmacro MUI_INSTALLOPTIONS_READ $1 "PageMrSoaSettings2.ini" "Field 2" "State"
    !insertmacro MUI_INSTALLOPTIONS_READ $2 "PageMrSoaSettings2.ini" "Field 4" "State"
    IfFileExists "$TOMCATPATH\webapps\admin.war" +1 +3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\admin.war" "WEB-INF\properties\jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\admin.war" "WEB-INF\properties\jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$TOMCATPATH\meinc\shout.war" +1 +3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\meinc\shout.war" "WEB-INF\properties\jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\meinc\shout.war" "WEB-INF\properties\jdbc.properties" "sconowsc0n0w" $2 $3
    IfFileExists "$TOMCATPATH\meinc\store.war" +1 +3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\meinc\store.war" "WEB-INF\properties\jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\meinc\store.war" "WEB-INF\properties\jdbc.properties" "sconowsc0n0w" $2 $3

    ;Replace in-war file 'support' user JDBC strings
    !insertmacro MUI_INSTALLOPTIONS_READ $1 "PageMrSoaSettings2.ini" "Field 10" "State"
    !insertmacro MUI_INSTALLOPTIONS_READ $2 "PageMrSoaSettings2.ini" "Field 12" "State"
    IfFileExists "$TOMCATPATH\webapps\phoenixBilling.war" +1 +5
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\phoenixBilling.war" "WEB-INF\classes\billing-hosted@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\phoenixBilling.war" "WEB-INF\classes\billing-hosted@jdbc.properties" "support5upp0r7" $2 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\phoenixBilling.war" "WEB-INF\classes\billing@jdbc.properties" "localhost\:3306" $1 $3
    ${ReplaceJarFileString} $0 "$TOMCATPATH\webapps\phoenixBilling.war" "WEB-INF\classes\billing@jdbc.properties" "support5upp0r7" $2 $3
  ${EndIf}

  ;Create miscellaneous directories
  CreateDirectory "$INSTDIR\www\downloads"

  SetOutPath "$INSTDIR\www\$SERVER_NAME"
  File /a "${ECLIPSE_WORKSPACE_DIR}\ShoutAdminSite\resources\shout.gif"

  CreateDirectory "$INSTDIR\www\accounts\_templates"
  SetOutPath "$INSTDIR\www\accounts\_templates"
  File /a /r /x .svn "${ECLIPSE_WORKSPACE_DIR}\ShoutAdminSite\resources\LandingPageTemplates\*.*"

  CreateDirectory "$INSTDIR\www\accounts\shout"
  SetOutPath "$INSTDIR\www\accounts\shout"
  File /a /r /x .svn "${ECLIPSE_WORKSPACE_DIR}\Shout2Web\src\resources\templates"

  SetOutPath "$INSTDIR\www\$SERVER_NAME"
  File /a /r /x .svn "${ECLIPSE_WORKSPACE_DIR}\Shout2Web\src\resources\toplevel\images"
  File /a /r /x .svn "${ECLIPSE_WORKSPACE_DIR}\Shout2Web\src\resources\toplevel\scripts"
  File /a "${ECLIPSE_WORKSPACE_DIR}\Shout2Web\src\resources\toplevel\*.*"

  CreateDirectory "$INSTDIR\www\$SERVER_NAME\meincsupport"
  SetOutPath "$INSTDIR\www\$SERVER_NAME\meincsupport"
  File /a "${ECLIPSE_WORKSPACE_DIR}\PhoenixAdminSite\resources\*.xls"

  CreateDirectory "$INSTDIR\uploads"
  CreateDirectory "$INSTDIR\www\shout2"

  CreateDirectory "$INSTDIR\www\ads"
  SetOutPath "$INSTDIR\www\ads"
  File /a "${ECLIPSE_WORKSPACE_DIR}\AdService\doc\default_r*.jpg"

  ;Unpack wav2mp3 utlities
  SetOutPath "$INSTDIR"
  File /a /r "${SOURCE_DIR}\wav2mp3"

  SectionGetFlags ${SecMySQL} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $9 != 0
    ;Unpack MrSOA SQL Scripts
    CreateDirectory "$MYSQLPATH\mrsoa-sql"
    SetOutPath "$MYSQLPATH\mrsoa-sql"
    File "${ECLIPSE_WORKSPACE_DIR}\AdService\doc\db\adservice_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\AggregationTeamService\doc\db\aggregationteam_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\ApplicationClientService\doc\db\appclient_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\ApplicationClientService\doc\db\appclient_init.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\AuthorizationService\doc\db\authorization_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\BillingService\doc\db\billing_simplified_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\DynamicBranding\doc\db\dynamicBranding_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\HostedTeamService\doc\db\hostedengine_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\HostedTeamService\doc\db\hostedengine_init_data.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\HostedTeamService\doc\db\hostedengine_reserved_account_keys.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\MintService\doc\db\mintservice_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\MyCoApplication\doc\db\myco_ddl.sql"
    ;File "${ECLIPSE_WORKSPACE_DIR}\OpusApplication\doc\db\opus_1.0_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\PhoenixCommons\doc\db\commons_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\PhoenixPaymentService\doc\db\ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\PhoenixService\doc\db\roadrunner_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\SCOIntegrationService\doc\atl_extension.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\SCOTeamService\doc\db\scoteam_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\Shout2Application\doc\db\shout2_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\ShoutApplication\doc\db\shout_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\VoteApplication\doc\db\vote_ddl.sql"
    File "${ECLIPSE_WORKSPACE_DIR}\Vote2Service\doc\db\vote2_ddl.sql"

    ;Execute MrSOA SQL Scripts
    SetOutPath "$MYSQLPATH\bin"
    ${If} $MYSQL_ROOT_PASS == ""
    ${OrIf} $MYSQL_ROOT_PASS_FAILED != ""
      StrCpy $0 ''
    ${Else}
      StrCpy $0 '-p"$MYSQL_ROOT_PASS"'
    ${EndIf}

    Var /GLOBAL MYSQL_SCRIPT_RESULTS
    StrCpy $MYSQL_SCRIPT_RESULTS 0

    DetailPrint "Starting MySQL"
    nsExec::ExecToLog 'NET START "$MYSQL_SERVICE_NAME"'
    Sleep 4000

    DetailPrint "Executing SQL Script commons_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\commons_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script adservice_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\adservice_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script aggregationteam_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\aggregationteam_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script appclient_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\appclient_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script appclient_init"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\appclient_init.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script authorization_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\authorization_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script billing_simplified_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\billing_simplified_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script dynamicBranding_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\dynamicBranding_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script hostedengine_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\hostedengine_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script hostedengine_init_data"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\hostedengine_init_data.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script hostedengine_reserved_account_keys"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\hostedengine_reserved_account_keys.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script mintservice_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\mintservice_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script myco_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\myco_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    ;DetailPrint "Executing SQL Script opus_1.0_ddl"
    ;nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\opus_1.0_ddl.sql"'
    ;Pop $1
    ;IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script roadrunner_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\roadrunner_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script atl_extension"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\atl_extension.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script scoteam_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\scoteam_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script shout2_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\shout2_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script shout_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\shout_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script vote_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\vote_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1
    DetailPrint "Executing SQL Script vote2_ddl"
    nsExec::ExecToLog '.\mysql -u root $0 -e "SOURCE ..\mrsoa-sql\vote2_ddl.sql"'
    Pop $1
    IntOp $MYSQL_SCRIPT_RESULTS $MYSQL_SCRIPT_RESULTS + $1

    ;Configure MySQL Users
    Var /GLOBAL MYSQL_CONFIG_RESULTS
    StrCpy $MYSQL_CONFIG_RESULTS 0

    Var /GLOBAL MS_SU_PASS
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_SU_PASS "PageMrSoaSettings.ini" "Field 4" "State"
    DetailPrint "Setting MrSOA Superuser Password"
    nsExec::ExecToLog `.\mysql -u root $0 -e "UPDATE meinc_hosted.subscriber SET passwd=PASSWORD('$MS_SU_PASS') WHERE subscriber_id=2"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    Var /GLOBAL MS_SCONOWPASS
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_SCONOWPASS "PageMrSoaSettings.ini" "Field 10" "State"
    DetailPrint "Creating MySQL User 'sconow'"
    nsExec::ExecToLog `.\mysql -u root $0 -e "GRANT ALL PRIVILEGES ON *.* TO 'sconow'@'%' IDENTIFIED BY '$MS_SCONOWPASS';"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    Var /GLOBAL MS_BILLINGPASS
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_BILLINGPASS "PageMrSoaSettings.ini" "Field 12" "State"
    DetailPrint "Creating MySQL User 'billing'"
    nsExec::ExecToLog `.\mysql -u root $0 -e "GRANT ALL PRIVILEGES ON billing.* TO 'billing'@'%' IDENTIFIED BY '$MS_BILLINGPASS';"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1
    nsExec::ExecToLog `.\mysql -u root $0 -e "GRANT SELECT ON meinc_hosted.* TO 'billing'@'%';`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    Var /GLOBAL MS_SUPPORTPASS
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_SUPPORTPASS "PageMrSoaSettings.ini" "Field 14" "State"
    DetailPrint "Creating MySQL User 'support'"
    nsExec::ExecToLog `.\mysql -u root $0 -e "GRANT SELECT ON meinc_hosted.* TO 'support'@'%' IDENTIFIED BY '$MS_SUPPORTPASS';"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    Var /GLOBAL MS_SERVER_NAME
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_SERVER_NAME "PageMrSoaSettings.ini" "Field 6" "State"
    DetailPrint "Setting MrSOA Server Host Name"
    nsExec::ExecToLog `.\mysql -u root $0 -e "UPDATE roadrunner.system_prefs SET value='$MS_SERVER_NAME' WHERE name='account base url'"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    Var /GLOBAL MS_EMAIL_SERVER
    !insertmacro MUI_INSTALLOPTIONS_READ $MS_EMAIL_SERVER "PageMrSoaSettings.ini" "Field 8" "State"
    DetailPrint "Setting MrSOA Mail Server Host Name"
    nsExec::ExecToLog `.\mysql -u root $0 -e "UPDATE roadrunner.system_prefs SET value='$MS_EMAIL_SERVER' WHERE name='email server'"`
    Pop $1
    IntOp $MYSQL_CONFIG_RESULTS $MYSQL_CONFIG_RESULTS + $1

    DetailPrint "Stopping MySQL"
    Sleep 1000
    nsExec::ExecToLog 'NET STOP "$MYSQL_SERVICE_NAME"'
  ${EndIf}
SectionEnd

Section -Post
  SectionGetFlags ${SecMrSoaServices} $8
  IntOp $8 $8 & ${SF_SELECTED}
  SectionGetFlags ${SecMySQL} $9
  IntOp $9 $9 & ${SF_SELECTED}
  ${If} $8 != 0
  ${AndIf} $9 != 0
    ${If} $MYSQL_ROOT_PASS_FAILED != ""
      MessageBox MB_ICONEXCLAMATION|MB_OK "A root password could not be set for MySQL.$\nYou will have to set the root password manually.$\nSee the log area of this window for details."
    ${EndIf}

    ${If} $MYSQL_SCRIPT_RESULTS != ""
    ${AndIf} $MYSQL_SCRIPT_RESULTS != 0
      MessageBox MB_ICONEXCLAMATION|MB_OK "One or more MySQL SQL Scripts failed during execution.$\nSee the log area of this window for details."
    ${EndIf}

    ${If} $MYSQL_CONFIG_RESULTS != ""
    ${AndIf} $MYSQL_CONFIG_RESULTS != 0
      MessageBox MB_ICONEXCLAMATION|MB_OK "One or more MySQL SQL Configuration Steps failed during execution.$\nSee the log area of this window for details."
    ${EndIf}
  ${EndIf}

  ;Store installation folder
  WriteRegStr HKCU "Software\MrSOA" "" $INSTDIR

;  ;Create uninstaller
;  WriteUninstaller "$INSTDIR\www\Uninstall-MrSOA-${VERSION}.exe"
SectionEnd

Function .onSelChange
;  ; MrSOA Services requires MrSOA Container
;  SectionGetFlags ${SecMrSoaServices} $1
;  SectionGetFlags ${SecMrSoaContainer} $2
;  ${If} $1 = 1
;  ${AndIf} $2 = 0
;    SectionSetFlags ${SecMrSoaContainer} ${SF_SELECTED}
;  ${EndIf}
;
;  ; MrSOA Services requires Tomcat
;  SectionGetFlags ${SecMrSoaServices} $1
;  SectionGetFlags ${SecTomcat} $2
;  ${If} $1 = 1
;  ${AndIf} $2 = 0
;    SectionSetFlags ${SecTomcat} ${SF_SELECTED}
;  ${EndIf}
;
;  ; MrSOA Services requires MySQL
;  ;SectionGetFlags ${SecMrSoaServices} $1
;  ;SectionGetFlags ${SecMySQL} $2
;  ;${If} $1 = 1
;  ;${AndIf} $2 = 0
;  ;  SectionSetFlags ${SecMySQL} ${SF_SELECTED}
;  ;${EndIf}
;
;  ; MrSOA Services requires Apache
;  SectionGetFlags ${SecMrSoaServices} $1
;  SectionGetFlags ${SecApache} $2
;  ${If} $1 = 1
;  ${AndIf} $2 = 0
;    SectionSetFlags ${SecApache} ${SF_SELECTED}
;  ${EndIf}

  ; If no JDK is preinstalled
  ReadEnvStr $0 JAVA_HOME
  ${If} $0 == ""
     ; MrSOA Container requires JDK
     SectionGetFlags ${SecMrSoaContainer} $1
     SectionGetFlags ${SecJavaJDK} $2
     ${If} $1 = 1
     ${AndIf} $2 = 0
       SectionSetFlags ${SecJavaJDK} ${SF_SELECTED}
     ${EndIf}

     ; Tomcat Container requires JDK
     SectionGetFlags ${SecTomcat} $1
     SectionGetFlags ${SecJavaJDK} $2
     ${If} $1 = 1
     ${AndIf} $2 = 0
       SectionSetFlags ${SecJavaJDK} ${SF_SELECTED}
     ${EndIf}
  ${EndIf}
FunctionEnd

Function .onInit
  ReadEnvStr $0 JAVA_HOME
  ${If} $0 != ""
    SectionSetFlags ${SecJavaJDK} 0
  ${EndIf}

  ;Extract InstallOptions INI files
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageInstallPassword.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageApacheSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageTomcatSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageMySQLSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageMrSoaContainerSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageMrSoaSettings.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageMrSoaSettings2.ini"
FunctionEnd

Function PageInstallPassword
  !insertmacro MUI_HEADER_TEXT "Install Password" "Enter the password for installation"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageInstallPassword.ini"
FunctionEnd

Function ValidateInstallPassword
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "PageInstallPassword.ini" "Field 3" "State"
  ${If} $0 != 'sconowsc0n0w'
    Sleep 3000
    MessageBox MB_ICONEXCLAMATION|MB_OK "Invalid Password"
    Quit
  ${EndIf}
FunctionEnd

Function PageApacheSettings
  ;If Apache is not to be installed do not show this page
  SectionGetFlags ${SecApache} $0
  IntOp $0 $0 & ${SF_SELECTED}

  ${If} $0 = 0
    Abort
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "Apache Server Settings" "Configure your new Apache Server"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageApacheSettings.ini" "Field 2" "State" "${SERVICE_PREFIX}${APACHE_SERVICE}"

  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageApacheSettings.ini"
FunctionEnd

Function PageTomcatSettings
  ;If Tomcat is not to be installed do not show this page
  SectionGetFlags ${SecTomcat} $0
  IntOp $0 $0 & ${SF_SELECTED}

  ${If} $0 = 0
    Abort
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "Tomcat Server Settings" "Configure your new Tomcat Server"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageTomcatSettings.ini" "Field 2" "State" "${SERVICE_PREFIX}${TOMCAT_SERVICE}"

  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageTomcatSettings.ini"
FunctionEnd

Function PageMySQLSettings
  ;If MySQL is not to be installed do not show this page
  SectionGetFlags ${SecMySQL} $0
  IntOp $0 $0 & ${SF_SELECTED}

  ${If} $0 = 0
    Abort
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "MySQL Databse Settings" "Configure your new MySQL Database"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMySQLSettings.ini" "Field 2" "State" "${SERVICE_PREFIX}${MYSQL_SERVICE}"

  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageMySQLSettings.ini"
FunctionEnd

Function PageMrSoaContainerSettings
  ;If MrSoaContainer is not to be installed do not show this page
  SectionGetFlags ${SecMrSoaContainer} $0
  IntOp $0 $0 & ${SF_SELECTED}
  ;If MrSoaSettings *is* to be installed do not show this page
  SectionGetFlags ${SecMrSoaServices} $1
  IntOp $1 $1 & ${SF_SELECTED}

  ${If} $0 = 0
  ${OrIf} $1 = 1
    Abort
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "MrSOA Container Settings" "Configure your new MrSOA Container"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaContainerSettings.ini" "Field 2" "State" "${SERVICE_PREFIX}"

  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageMrSoaContainerSettings.ini"
FunctionEnd

Function PageMrSoaSettings
  ;If MrSOA Services is not to be installed do not show this page
  SectionGetFlags ${SecMrSoaServices} $0
  IntOp $0 $0 & ${SF_SELECTED}

  ${If} $0 = 0
    Abort
  ${EndIf}

  SectionGetFlags ${SecMySQL} $0
  IntOp $0 $0 & ${SF_SELECTED}
  ${If} $0 = 0
    !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings.ini" "Field 10" "Flags" DISABLED
    !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings.ini" "Field 12" "Flags" DISABLED
    !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings.ini" "Field 14" "Flags" DISABLED
  ${EndIf}

  ;Transfer host name entered for Apache to MrSOA
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "PageApacheSettings.ini" "Field 4" "State"
  ${If} $0 != ""
    !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings.ini" "Field 6" "State" $0
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "MrSOA Service Settings" "Configure your new MrSOA Services"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings.ini" "Field 2" "State" "${SERVICE_PREFIX}"

  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageMrSoaSettings.ini"
FunctionEnd

Function PageMrSoaSettings2
  ;If MrSOA Services is not to be installed do not show this page
  SectionGetFlags ${SecMrSoaServices} $0
  IntOp $0 $0 & ${SF_SELECTED}
  SectionGetFlags ${SecMrSoaContainer} $1
  IntOp $1 $1 & ${SF_SELECTED}

  ${If} $0 = 0
  ${OrIf} $1 = 0
    Abort
  ${EndIf}

  ;Transfer info entered for MySQL to MrSOA
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "PageMrSoaSettings.ini" "Field 10" "State"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings2.ini" "Field 4" "State" $0

  !insertmacro MUI_INSTALLOPTIONS_READ $0 "PageMrSoaSettings.ini" "Field 12" "State"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings2.ini" "Field 8" "State" $0

  !insertmacro MUI_INSTALLOPTIONS_READ $0 "PageMrSoaSettings.ini" "Field 14" "State"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "PageMrSoaSettings2.ini" "Field 12" "State" $0

  ;Display page
  !insertmacro MUI_HEADER_TEXT "MrSOA Service Settings Continued" "Configure your new MrSOA Services"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageMrSoaSettings2.ini"
FunctionEnd

Function StopMySqlMessage
  SectionGetFlags ${SecMySQL} $0
  IntOp $0 $0 & ${SF_SELECTED}
  ${If} $0 != 0
     MessageBox MB_OK "Please stop any existing instances of MySQL Server before continuing"
  ${EndIf}
FunctionEnd

Function .onInstSuccess
FunctionEnd

;--------------------------------
;Descriptions

;  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMrSoaServices} "Configure the components selected above to work with the standard MrSOA Services"
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

;Section "Uninstall"
;
;  ;ADD YOUR OWN FILES HERE...
;
;  RMDir /r "$INSTDIR\www\osgi-r4"
;
;  Delete "$INSTDIR\www\Uninstall-MrSOA-${VERSION}.exe"
;
;  RMDir "$INSTDIR"
;
;  DeleteRegKey /ifempty HKCU "Software\Modern UI Test"
;
;SectionEnd
