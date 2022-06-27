For MrSOA 3.0 to work properly, add the following lines to the bottom of the 
Java Options text area in the Java tab of the Tomcat System Tray Properties 
Dialog:

-Dmrsoa.client.localhost.fallback=true
-Dmeinc.server.properties.file=c:\mrsoa142\mrsoa.properties