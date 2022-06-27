package com.meinc.launcher.inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.meinc.launcher.deployer.ArchiveDeployer;
import com.meinc.launcher.deployer.ArchiveDeployer.JarOperationFailed;

public class InjectBytecodeIntoJar {
  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.err.println(
          InjectBytecodeIntoJar.class.getName() +
          " target_jar_path fully_qualified_class method_name method_arg_count");
      System.exit(1);
    }
    
    String target_jar = args[0];
    File targetJar = new File(target_jar);
    if (!targetJar.exists() || !targetJar.isFile()) {
      System.err.println("Specified JAR file does not exist");
      System.exit(1);
    }
      
    String class_desc = args[1];
    String class_path = class_desc.replaceAll("\\.", "/") + ".class";
    
    String method_name = args[2];
    
    int method_arg_count = Integer.parseInt(args[3]);
    
    /* Extract class file from JAR */
    
    String currentDirString = System.getProperty("user.dir");
    File currentDir = new File(currentDirString);
    File tempDir = new File(currentDir, "temp");
    if (!(tempDir.exists() && tempDir.isDirectory()) && !tempDir.mkdir()) {
      System.err.println("Could not create temp dir at " + tempDir);
      System.exit(1);
    }
    
    System.out.println("Extracting " + class_path);
    
    File newFile = null;
    try {
      newFile = ArchiveDeployer.extractJarFile(targetJar, class_path, tempDir, true);
    } catch (JarOperationFailed e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    
    System.out.println("Scanning " + class_path);
    
    FileInputStream newFileIn = new FileInputStream(newFile);
    
    ClassReader cr = new ClassReader(newFileIn);
    ClassWriter cw = new ClassWriter(cr, 0);
    ClassModifier cm = new ClassModifier(cw, method_name, method_arg_count);
    cr.accept(cm, 0);
    byte[] classBytes = cw.toByteArray();
    
    newFileIn.close();
    
    if (!MethodModifier.wasInjected()) {
      System.out.println("No bytecode was injected - aborting");
    } else {
      FileOutputStream newFileOut = new FileOutputStream(newFile);
      newFileOut.write(classBytes);
      newFileOut.close();
      
      System.out.println("Replacing original " + class_path);
      
      try {
        updateFileIntoJar(targetJar, class_path, tempDir);
      } catch (JarOperationFailed e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      
      System.out.println("Operation Successful");
    }
    
    ArchiveDeployer.rmDashRF(tempDir);
  }
  
  public static void updateFileIntoJar(File jarFile, String fileName, File workDir) throws JarOperationFailed {
    File jarExe = ArchiveDeployer.getJarExeFile();
    
    ProcessBuilder procBuilder = new ProcessBuilder(
        jarExe.getAbsolutePath(),
        "-uf",
        jarFile.getAbsolutePath(),
        fileName);
    
    procBuilder.directory(workDir);
    Process proc;
    try {
      proc = procBuilder.start();
    } catch (IOException e) {
      throw new JarOperationFailed("Could not update file into JAR", e);
    }
    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      throw new JarOperationFailed("Could not update file into JAR", e);
    }
  }
}
