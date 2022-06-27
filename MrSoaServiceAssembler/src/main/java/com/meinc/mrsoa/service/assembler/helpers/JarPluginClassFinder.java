package com.meinc.mrsoa.service.assembler.helpers;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarPluginClassFinder {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            throw new RuntimeException("Please specify JAR path and filename as first argument");
        String jarFilePath = args[0];
        JarFile jarFile = new JarFile(jarFilePath);
        System.out.println(findJarPluginFactoryClass(jarFile));
    }

    public static String findJarPluginFactoryClass(JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();
        if (manifest == null)
            throw new RuntimeException("Specified JAR file has no manifest file");
        Attributes attrs = manifest.getMainAttributes();
        if (attrs.containsKey(new Attributes.Name("MrSOA-Plugin-Factory"))) {
            return (String) attrs.get(new Attributes.Name("Plugin-Factory-Class"));
        }
        throw new RuntimeException("No Plugin Factory could be found in the JAR file " + jarFile.getName());
    }
}
