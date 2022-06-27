package com.meinc.mrsoa.service.assembler.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class GenerateMasterBundleActivator {
    public static final String MASTER_ACTIVATOR_FILENAME = "MasterActivator.java";
    public static final String ACTIVATOR_CONTAINER_FILENAME = "ActivatorContainer.java";

    public static void generateActivator(String servicePackageName, String outputPath, String[] activators) {
        List<String> activatorsList = Arrays.asList(activators);
        //TODO: modify apt processor to return service-name *and* stub-file-path so we don't have to guess at the service name
        String serviceName = activators[0].substring(activators[0].lastIndexOf('.')+1);
        serviceName = serviceName.replaceFirst("Stub$", "");

        try {
            Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init();
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
        VelocityContext context = new VelocityContext();
        context.put("activators", activatorsList);
        context.put("servicePackageName", servicePackageName);
        context.put("serviceName", serviceName);

        Writer writer1 = null;
        try {
            File outputFilePath = new File(outputPath, "com/meinc/mrsoa/service/stubs/" + servicePackageName);
            File outputFile = new File(outputFilePath, serviceName + ACTIVATOR_CONTAINER_FILENAME);
            if (!outputFilePath.exists() && !outputFilePath.mkdirs())
                throw new RuntimeException("Failed to create output directory");
            writer1 = new FileWriter(outputFile, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Template template1;
        try {
            template1 = Velocity.getTemplate("com/meinc/mrsoa/service/assembler/helpers/mrsoa-activator-container.java.vm");
            template1.merge(context, writer1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            writer1.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Writer writer2 = null;
        try {
            File outputFilePath = new File(outputPath, "com/meinc/mrsoa/service/stubs/" + servicePackageName);
            File outputFile = new File(outputFilePath, MASTER_ACTIVATOR_FILENAME);
            if (!outputFilePath.exists() && !outputFilePath.mkdirs())
                throw new RuntimeException("Failed to create output directory");
            writer2 = new FileWriter(outputFile, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Template template2;
        try {
            template2 = Velocity.getTemplate("com/meinc/mrsoa/service/assembler/helpers/mrsoa-master-activator.java.vm");
            template2.merge(context, writer2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            writer2.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new RuntimeException("Please specify output path as first argument and ");

        String serviceName = args[0];
        String outputPath = args[1];
        String activators = args[2];

        String[] activatorsArray = activators.split(",");
        generateActivator(serviceName, outputPath, activatorsArray);
    }
}
