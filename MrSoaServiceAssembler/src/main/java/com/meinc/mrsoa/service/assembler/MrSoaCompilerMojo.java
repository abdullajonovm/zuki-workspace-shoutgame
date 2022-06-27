package com.meinc.mrsoa.service.assembler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.compiler.AbstractCompilerMojo;
import org.apache.maven.plugin.compiler.CompilationFailureException;
import org.apache.maven.plugin.compiler.CompilerMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.meinc.mrsoa.service.assembler.helpers.JarPluginClassFinder;

@Mojo( name                         = "compile-service",
       defaultPhase                 = LifecyclePhase.COMPILE,
       threadSafe                   = true,
       requiresDependencyResolution = ResolutionScope.COMPILE )
public class MrSoaCompilerMojo extends CompilerMojo {
    public static String fileSeparatorRegex = "(?:/|"+Pattern.quote("\\")+")";
    public static String fileSeparatorReplaceRegex = File.separator.replaceAll(Pattern.quote("\\"), "\\\\\\\\");
        
    @Parameter(defaultValue = "${project.properties}",
               readonly     = true)
    private Properties projectProps;

    @Parameter(defaultValue="${project.dependencyArtifacts}", readonly=true, required=true)
    private Set<Artifact> projectDependencyArtifacts;

    @Parameter(defaultValue = "${settings.localRepository}",
               readonly     = true)
    private String localRepo;

    @Override
    public void execute() throws MojoExecutionException, CompilationFailureException {
        setState();
        setAnnotationProcessors(new String[] { MrSoaServiceAnnotationProcessor.class.getName() });
        super.execute();
    }
    
    private void setAnnotationProcessors(String[] annotationProcessors) throws MojoExecutionException {
        Field annotationProcessorsField;
        try {
            annotationProcessorsField = AbstractCompilerMojo.class.getDeclaredField("annotationProcessors");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        annotationProcessorsField.setAccessible(true);
        try {
            annotationProcessorsField.set(this, annotationProcessors);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void setState() throws MojoExecutionException {
        Map<String,String> pluginParmsMap = new HashMap<>();
        String pluginDependencyString = null;
        for (Object propKeyObj : projectProps.keySet()) {
            String propKey = (String) propKeyObj;
            if ("plugin.dependency".equals(propKey))
                pluginDependencyString = projectProps.getProperty(propKey);
            else if (propKey.startsWith("plugin."))
                pluginParmsMap.put(propKey.substring("plugin.".length()), projectProps.getProperty(propKey));
        }

        String pluginFactoryClassName = null;
        if (pluginDependencyString != null) {
            String[] pluginParts = pluginDependencyString.split(":");
            if (pluginParts.length != 3)
                throw new MojoExecutionException("Invalid plugin.dependency property - group, artifact & version required: " + pluginDependencyString);

            Artifact artifact = projectDependencyArtifacts.stream()
                                                              .filter(a -> a.getGroupId().equals(pluginParts[0]) &&
                                                                  a.getArtifactId().equals(pluginParts[1]) &&
                                                                  a.getVersion().equals(pluginParts[2]))
                                                              .findFirst().orElse(null);
            if (artifact == null)
                throw new MojoExecutionException("'plugin.dependency' provided but was not used as a dependency: " + pluginDependencyString);

            try {
                pluginFactoryClassName = JarPluginClassFinder.findJarPluginFactoryClass(new JarFile(artifact.getFile().getPath()));
            } catch (IOException e) {
                throw new MojoExecutionException("Error while reading JAR file: " + artifact.getFile().getPath() + " -> " + e.getMessage());
            }
        }

        MrSoaMojoSharedState state = MrSoaMojoSharedState.readState();
        state.pluginFactoryClassName = pluginFactoryClassName;
        state.pluginParms = pluginParmsMap;
        state.generatedSourcesFile = getGeneratedSourcesDirectory();
        MrSoaMojoSharedState.saveState(state);
    }
}
