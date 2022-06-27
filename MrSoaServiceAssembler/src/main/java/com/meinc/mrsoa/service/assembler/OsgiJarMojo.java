package com.meinc.mrsoa.service.assembler;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;

import domain.Osgi;

/**
 * Maven goal that generates an OSGI bundle.
 * 
 * @author Matt
 */
@Mojo( name         = "package-osgi",
       defaultPhase = PACKAGE,
       threadSafe   = true )
public class OsgiJarMojo extends AbstractMojo {

    @Parameter
    private Osgi osgi;

    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;

    @Parameter(defaultValue="${project.build.directory}", readonly=true, required=true)
    private String projectTargetPath;

    @Parameter( defaultValue="${project.dependencyArtifacts}", readonly=true, required=true)
    private Set<Artifact> projectDependencyArtifacts;

    @Parameter(defaultValue="${project.build.outputDirectory}", readonly=true)
    private String projectTargetClassesPath;

    @Component(role=Archiver.class, hint="jar")
    private JarArchiver osgiJarArchiver;
    
    @Parameter
    private MavenArchiveConfiguration osgiArchiveConfig = new MavenArchiveConfiguration();

    @Parameter(alias="jarName", property="jar.finalName", defaultValue="${project.build.finalName}")
    private String finalName;
    
    @Parameter(defaultValue="${session}", readonly=true, required=true)
    private MavenSession session;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        /* Read and format project's OSGI configuration values */
        String osgiRequireString = null;
        if (osgi.require != null && !osgi.require.isEmpty()) {
            osgiRequireString = osgi.require.replaceAll("\\s|\r|\n", "");
            osgiArchiveConfig.addManifestEntry("Require-Bundle", osgiRequireString);
        }

        String osgiExportString = null;
        if (osgi.export != null && !osgi.export.isEmpty()) {
            osgiExportString = osgi.export.replaceAll("\\s|\r|\n", "");
            if (!osgiExportString.isEmpty())
                osgiArchiveConfig.addManifestEntry("Export-Package", osgiExportString);
        }

        String osgiActivator = null;
        if (osgi.activator != null && !osgi.activator.isEmpty()) {
            osgiActivator = osgi.activator.replaceAll("\\s|\r|\n", "");
            osgiArchiveConfig.addManifestEntry("Bundle-Activator", osgiActivator);
        }

        List<Artifact> bundleArtifacts = projectDependencyArtifacts.stream()
                                                                       .filter(a -> "compile".equals(a.getScope()) || "runtime".equals(a.getScope()))
                                                                       .collect(toList());

        for (Artifact bundleArtifact : bundleArtifacts)
            osgiJarArchiver.addFile(bundleArtifact.getFile(), bundleArtifact.getFile().getName());
        
        String osgiBundleClasspath = ".," + bundleArtifacts.stream()
                                                               .map(a->a.getFile().getName())
                                                               .collect(joining(","));
        osgiArchiveConfig.addManifestEntry("Bundle-Classpath", osgiBundleClasspath);
        
        osgiArchiveConfig.addManifestEntry("Bundle-SymbolicName", project.getArtifactId());
        osgiArchiveConfig.addManifestEntry("Bundle-Version", project.getVersion());

        File projectTargetClassesFile = new File(projectTargetClassesPath);
        if (projectTargetClassesFile != null && projectTargetClassesFile.exists())
            osgiJarArchiver.addDirectory(projectTargetClassesFile, new String[] {"**/**"}, null);

        String serviceJarName = finalName + ".jar";
        File osgiJarFile = new File(projectTargetPath, serviceJarName);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(osgiJarArchiver);
        archiver.setOutputFile(osgiJarFile);
        
        try {
            archiver.createArchive(session, project, osgiArchiveConfig);
        } catch (ArchiverException | ManifestException | IOException | DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        project.getArtifact().setFile(osgiJarFile);
    }
}
