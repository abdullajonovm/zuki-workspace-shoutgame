This directory contains the NSIS installer scripts used to build the Mr.SOA
installer.  The main installer script is MrSoa.nsi.  This script requires an
installer-source directory on the machine where the installer is constructed.
This directory must contain all of the application servers and their respective
services/webapps.  View the global variables at the top of this script to see
the paths currently expected.  At time of writing, the following is the
directory structure expected:


C:\installer-source\
   vcredist_x86-sp1.exe (see ApacheLounge's README for link)
   dev\
      jdk1.6.0_01\
         Standard JDK 1.6.0_01
      mysql5027\
         Standard MySQL 5.0.27
         Templatized my.ini
   wav2mp3\
      Our collection of wav2mp3 files from SVN
         (See PhoenixBuild/doc/wav2mp3)
   www\
      apache224\
         Standard Apache 2.2.4 from apachelounge.com
         conf\
            Templatized httpd.conf from SVN
            Templatized openssl.cfg from SVN
            ssl.conf from SVN
            workers.properties from SVN
         modules\
            mod_jk.so from SVN
            mod_log_rotate.so from SVN
      osgi-r4\
         bin\
            export-packages-to-classpath.txt from SVN
            framework.policy from knopflerfish.jar
            meinc.xargs from SVN
            run.bat from SVN
            run-debug.bat from SVN
            Windows Service\
               installOsgi.bat from SVN
               JavaService.exe from javaservice.objectweb.org
               JavaServiceDebug.exe from javaservice.objectweb.org
               test_osgi_install.bat from SVN
               test_osgi_uninstall.bat from SVN
         bundles\
            Copied all from knopflerfish.jar:knopflerfish.org\osgi\jars\
            osgi-activemq-4.1.0.jar
            osgi-commons-collections-3.1.jar
            osgi-commons-logging-1.1.1.jar
            osgi-commons-pool-1.3.jar
            osgi-mybatis-3.0.4.jar
            osgi-jms-monitor-1.1.jar
            osgi-producer-pool-1.1.jar
            osgi-springframework-3.0.5.RELEASE.jar
         deploy\
            [All of our Phoenix/MeInc service JAR's]
         lib\
            framework.jar from knopflerfish.jar
            log4j-1.2.8.jar
				log4j-tools-1.0.jar from SVN Log4jTools project
            log4j.xml from SVN
      tomcat6010\
			Standard Tomcat 6.0.10 from ZIP file (not installer) with Tomcat
				extras compiled from Tomcat 6.0.10 source distribution
			bin\
				tomcat-juli.jar replaced from Tomcat extras
         conf\
            context.xml from SVN
            server.xml from SVN
            Templatized tomcat-users.xml from SVN
         lib\
            log4j-1.2.8.jar
				log4j-tools-1.0.jar from SVN Log4jTools project
            log4j.xml from SVN
				tomcat-juli-adapters.jar from Tomcat extras
            activemq-4.1.0.jar
            producer-pool-1.3.jar
            osgi-jms-monitor-1.1.jar
            osgi-producer-pool-1.1.jar
         meinc\
            action.war
            shout.war
            store.war
         webapps\
            [All directories may be removed except manager\ and host-manager\]
            admin.war
            eps.war
            phoenixBilling.war
            postcard.war


In addition to the installer-source directory outlined above, the installer
script expects to find a copy of all the Phoenix/MeInc source projects checked
out of Subversion to a local path on the machine used to construct the
installer.  Change the ECLIPSE_WORKSPACE_DIR global variable in the MrSoa.nsi
script to point to this directory.

Note that some of the files outlined above are marked as "Templatized".  A
templatized configuration file contains special template variables that can be
modified during runtime installation to customize the configuration file to the
target machine's specific environment.  All of these templatized configuration
files are stored in Subversion in PhoenixBuild/doc/nsis/installer-source/.

Keeping the templatized configuration files in-sync with the "real"
configuration files (the other files in PhoenixBuild/doc/...) will be a
challenge going forward in the future.  If you find that one of the templatized
configuration files is severely out of date, it may be easier to copy the real
configuration file and templatize it anew.  Template variables are of the form
%%VARNAME where VARNAME is replaced by the actual template variable name.
