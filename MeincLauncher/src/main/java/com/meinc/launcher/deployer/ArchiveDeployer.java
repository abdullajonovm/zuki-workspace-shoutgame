package com.meinc.launcher.deployer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Launches a thread that watches a specified directory for JAR/WAR files.  When a
 * JAR/WAR file appears in the watched directory, deploy.xml is extracted from the
 * root of the JAR/WAR file (if it exists) and is executed using Ant.  This Ant
 * script is executed in a temporary directory and thus may use its
 * <code>basedir</code> freely.  Also, this Ant script may use the
 * <code>archive</code> property to locate, open, extract, modify, and
 * repackage its containing JAR/WAR file.
 * <h4>Java System Properties</h3>
 * <table>
 * <tr><td>meinc.deployer.path</td><td>The directory to watch for JAR/WAR files</td></tr>
 * <tr><td>meinc.deployer.container.deploy.path</td><td>Container's deployment directory</td></tr>
 * <tr><td>meinc.deployer.millis</td><td>Optional. The interval in milliseconds to check for new JAR/WAR files. Defaults to 3000.</td></tr>
 * <tr><td>meinc.deployer.ant.home</td><td>The home directory of Ant</td></tr>
 * </table>
 * <h4>Ant Properties</h3>
 * <table>
 * <tr><td>archive</td><td>The path to the JAR/WAR file from whence this Ant script was extracted</td></tr>
 * </table>
 *
 * @author Matt
 */
public class ArchiveDeployer extends Thread {
  private static ArchiveDeployer deployer;
  
  public static synchronized void startDeployer() {
    if (deployer != null) return;
    
    String deployerPathString = System.getProperty("meinc.deployer.path");
    String deployerContainerPathString = System.getProperty("meinc.deployer.container.deploy.path");
    String deployerMillisString = System.getProperty("meinc.deployer.millis");
    String deployerAntHome = System.getProperty("meinc.deployer.ant.home");
    
    if (deployerPathString == null || deployerPathString.trim().length() == 0
      || deployerContainerPathString == null || deployerContainerPathString.trim().length() == 0
      || deployerAntHome == null || deployerAntHome.trim().length() == 0) {
      String error = "'meinc.deployer.path' AND 'meinc.deployer.millis'"
        + " AND 'meinc.deployer.ant.home' AND 'meinc.deployer.container.deploy.path'"
        + " system properties must be defined";
      System.err.println(error);
      throw new IllegalStateException(error);
    }
    
    File deployerPath = new File(deployerPathString).getAbsoluteFile();
    if (!deployerPath.exists() || !deployerPath.isDirectory() || !deployerPath.canWrite()) {
      String error = "Cannot open '" + deployerPathString + "' direcory for writing";
      System.err.println(error);
      throw new IllegalStateException(error);
    }
    
    File deployerContainerPath = new File(deployerContainerPathString).getAbsoluteFile();
    if (!deployerContainerPath.exists()
        || !deployerContainerPath.isDirectory()
        || !deployerContainerPath.canWrite()) {
      String error = "Cannot open '" + deployerContainerPathString + "' direcory for writing";
      System.err.println(error);
      throw new IllegalStateException(error);
    }
    
    int deployerMillis = 3000; // the default
    if (deployerMillisString != null && deployerMillisString.trim().length() != 0)
      deployerMillis = Integer.parseInt(deployerMillisString);

    deployer = new ArchiveDeployer(deployerPath, deployerContainerPath, deployerMillis, deployerAntHome);
    deployer.start();
  }
  
  private File deployerPath;
  private File deployerContainerPath;
  private int deployerMillis;
  private String deployerAntHome;
  
  private ArchiveDeployer(File deployerPath, File deployerContainerPath, int deployerMillis, String deployerAntHome) {
    this.deployerPath = deployerPath;
    this.deployerContainerPath = deployerContainerPath;
    this.deployerMillis = deployerMillis;
    this.deployerAntHome = deployerAntHome;
    
    setDaemon(false);
    setName("Meinc Archive Deployer Thread");
  }
  
  @Override
  public void run() {
    try {
      
      Map<File,Long> ignoredFiles = new HashMap<File,Long>();
      
      while (!isInterrupted()) {
        
        /* Scan for new JAR/WAR files */
        
        Map<File,Long> newIgnoredFiles = new HashMap<File,Long>();
        File[] files = (File[]) deployerPath.listFiles();
        for (File file : files) {
          if (!file.isFile() || !file.canRead() || !file.canWrite()
              || (!file.getName().toLowerCase().endsWith(".war")
                  && !file.getName().toLowerCase().endsWith(".jar")))
            continue;

          if (ignoredFiles.containsKey(file) && ignoredFiles.get(file) == file.lastModified()) {
            newIgnoredFiles.put(file, file.lastModified());
            continue;
          }
          
          /* Found a JAR/WAR - create a temp directory just for it */
          
          File workPath = new File(deployerPath, "temp_" + file.getName());
          
          if (workPath.exists() && !rmDashRF(workPath)) {
            System.err.println("Temp directory for '" + file + "' already exists and cannot be removed");
            cleanup(file, workPath, newIgnoredFiles);
            continue;
          }
            
          if (!workPath.mkdir()) {
            System.err.println("Cannot create temp directory for file '" + file.toString() + "'");
            cleanup(file, workPath, newIgnoredFiles);
            continue;
          }
         
          /* Extract the deploy script from the JAR/WAR file */
          
          File deployFilePath;
          try {
            deployFilePath = extractJarFile(file, "deploy.xml", workPath, false);
            if (deployFilePath == null) {
              System.err.println("File '" + file.toString() + "' does not contain deploy.xml - deploying anyway");
              if (!moveFile(file, deployerContainerPath))
                System.err.println("Could not move file '" + file + "' to container's deployment directory");
              cleanup(file, workPath, newIgnoredFiles);
              continue;
            }
          } catch (JarOperationFailed e) {
            e.printStackTrace();
            continue;
          }
          
          /* Execute deploy script using Ant */
          
          ProcessBuilder procBuilder = new ProcessBuilder(
              "java",
              "-classpath",
              deployerAntHome + "/lib/ant-launcher.jar",
              "-Dant.home=" + deployerAntHome,
              "org.apache.tools.ant.launch.Launcher",
              "-buildfile",
              deployFilePath.toString(),
              "-Darchive=\"" + file.getPath() + "\"");
          
          procBuilder.directory(workPath);
          Process proc;
          try {
            proc = procBuilder.start();
          } catch (IOException e) {
            System.err.println("Could not launch Ant: " + e.getMessage());
            continue;
          }
          
          proc.waitFor();
          
          InputStream procIn = proc.getErrorStream();
          byte[] procBytes = new byte[256];
          int bytesRead = 0;
          while (true) {
            try {
              bytesRead = procIn.read(procBytes);
            } catch (IOException e) { break; }
            if (bytesRead == -1) break;
            System.err.write(procBytes, 0, bytesRead);
          }
          
          procIn = proc.getInputStream();
          bytesRead = 0;
          while (true) {
            try {
              bytesRead = procIn.read(procBytes);
            } catch (IOException e) { break; }
            if (bytesRead == -1) break;
            System.out.write(procBytes, 0, bytesRead);
          }
          
          if (proc.exitValue() != 0) {
            System.err.println("Error while running deploy.xml script - aborting");
            cleanup(file, workPath, newIgnoredFiles);
            continue;
          }
          
          /* Move the JAR/WAR file to Container's deployment directory */
          
          if (!moveFile(file, deployerContainerPath)) {
            System.err.println("Could not move file '"+file+"' to Container's deployment directory");
            continue;
          }
          
          /* Remove the temporary directory */
          
          cleanup(file, workPath, newIgnoredFiles);
        }
        
        ignoredFiles = newIgnoredFiles;
        
        Thread.sleep(deployerMillis);
      }
    } catch (InterruptedException e) {
    }
  }

  private void cleanup(File file, File workPath, Map<File,Long> ignoredFiles) {
    if (!rmDashRF(workPath)) {
      System.err.println("Could not delete temp directory for file '"+file+"' - attempting to rename it");
      boolean renamed = workPath.renameTo(new File(deployerPath, workPath.getName()+UUID.randomUUID()));
      if (!renamed)
        System.err.println("Could not rename temp directory for file '"+file+"' - this may cause problems if this file is ever deployed again!");
    }
    if (file.exists()) {
      ignoredFiles.put(file, file.lastModified());
    }
  }

  public static boolean rmDashRF(File directory) {
    boolean success = true;
    File[] files = (File[]) directory.listFiles();
    for (File file : files) {
      if (file.isDirectory())
        success = rmDashRF(file) && success;
      else
        success = file.delete() && success;
    }
    success = directory.delete() && success;
    return success;
  }

  public static File getJarExeFile() throws JarOperationFailed {
    String javaDirString = System.getProperty("java.home");
    if (javaDirString == null)
      throw new JarOperationFailed("Cannot locate Java installation directory");
    File jarExeDir = new File(javaDirString+"/bin");
    if (!jarExeDir.exists() || !jarExeDir.isDirectory())
      throw new JarOperationFailed("Cannot locate Java executable directory");
    
    File jarExe = new File(jarExeDir, "jar");
    if (!jarExe.exists() || !jarExe.isFile()) {
      // Try treating this JRE as a JDK
      jarExeDir = new File(javaDirString+"/../bin");
      if (!jarExeDir.exists() || !jarExeDir.isDirectory())
        throw new JarOperationFailed("Cannot locate Java executable directory");
      jarExe = new File(jarExeDir, "jar");
      if (!jarExe.exists() || !jarExe.isFile())
        throw new JarOperationFailed("Cannot locate jar");
    }
    
    return jarExe;
  }
  
  public static File extractJarFile(File jarFile, String fileName, File toPath, boolean noSuchFileIsError) throws JarOperationFailed {
    File jarExe = getJarExeFile();
    
    ProcessBuilder procBuilder = new ProcessBuilder(
        jarExe.getAbsolutePath(),
        "-xf",
        jarFile.getAbsolutePath(),
        fileName);
    
    procBuilder.directory(toPath);
    Process proc;
    try {
      proc = procBuilder.start();
    } catch (IOException e) {
      throw new JarOperationFailed("Could not extract file from JAR", e);
    }
    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      throw new JarOperationFailed("Could not extract file from JAR", e);
    }
    
    File newFile = new File(toPath, fileName);
    if (!newFile.exists()) {
      if (noSuchFileIsError)
        throw new JarOperationFailed("Could not extract file from JAR");
      else
        return null;
    }
    
    return newFile;
  }
  
  private boolean moveFile(File file, File toDirectory) {
    return file.renameTo(new File(toDirectory, file.getName()));
  }
  
  public static class JarOperationFailed extends Exception {
    public JarOperationFailed(String message) {
      super(message);
    }
    public JarOperationFailed(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
