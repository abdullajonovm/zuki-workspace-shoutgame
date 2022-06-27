package com.meinc.mrsoa.service.assembler;

import static com.meinc.mrsoa.service.assembler.MrSoaCompilerMojo.fileSeparatorRegex;
import static com.meinc.mrsoa.service.assembler.MrSoaCompilerMojo.fileSeparatorReplaceRegex;
import static com.meinc.mrsoa.service.assembler.MrSoaServiceAnnotationProcessor.OSGI_ACTIVATOR_CLASSNAME;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;

import domain.ClientJar;
import domain.Osgi;

/**
 * Maven goal that generates a MrSOA service bundle.
 */
@Mojo(name         = "package-service",
      defaultPhase = LifecyclePhase.PACKAGE,
      threadSafe   = true)
public class MrSoaPackagerMojo extends AbstractMojo {
    @Parameter
    private Osgi osgi;

    @Parameter(defaultValue="${project}", readonly=true)
    private MavenProject project;

    @Parameter(defaultValue="${project.build.outputDirectory}", readonly=true)
    private String projectTargetClassesPath;

    @Parameter(defaultValue="${project.build.directory}", readonly=true, required=true)
    private String projectTargetPath;

    @Parameter(defaultValue="${project.compileSourceRoots}", readonly=true, required=true)
    private List<String> projectCompileSrcRoots;

    @Parameter(defaultValue="${project.properties}", readonly=true)
    private Properties projectProps;

    @Parameter(defaultValue="${project.dependencies}", readonly=true, required=true)
    private List<Dependency> projectDependencies;

    @Parameter( defaultValue = "${project.dependencyArtifacts}", readonly = true, required = true)
    private Set<Artifact> projectDependencyArtifacts;

    @Parameter
    private ClientJar clientJar;

    @Component(role=Archiver.class, hint="jar")
    private JarArchiver serviceJarArchiver;

    @Component(role=Archiver.class, hint="jar")
    private JarArchiver clientJarArchiver;
    
    @Parameter(alias="jarName", property="jar.finalName", defaultValue="${project.build.finalName}")
    private String finalName;
    
    @Parameter(defaultValue="${session}", readonly=true, required = true)
    private MavenSession session;
    
    @Parameter
    private MavenArchiveConfiguration serviceArchiveConfig = new MavenArchiveConfiguration();

    @Parameter
    private MavenArchiveConfiguration clientArchiveConfig = new MavenArchiveConfiguration();
    
    @Component
    private MavenProjectHelper projectHelper;
    
    private Map<String, Object> restrictedDeps = new HashMap<String, Object>();

    public MrSoaPackagerMojo() {
        restrictedDeps.put("commons-logging", null);
        restrictedDeps.put("com.springsource.org.apache.commons.logging", null);
        restrictedDeps.put("commons-collections", null);
        restrictedDeps.put("commons-pool", null);
        restrictedDeps.put("mrsoa-kernel", null);
        restrictedDeps.put("ibatis", null);

        //TODO: put back in.
/*
        restrictedDeps.put("mybatis", null);
        restrictedDeps.put("spring", null);
        restrictedDeps.put("springframework", null);
        restrictedDeps.put("org.springframework.aop", null);
        restrictedDeps.put("org.springframework.asm", null);
        restrictedDeps.put("org.springframework.aspects", null);
        restrictedDeps.put("org.springframework.beans", null);
        restrictedDeps.put("org.springframework.context", null);
        restrictedDeps.put("org.springframework.context.support", null);
        restrictedDeps.put("org.springframework.core", null);
        restrictedDeps.put("org.springframework.expression", null);
        restrictedDeps.put("org.springframework.jdbc", null);
        restrictedDeps.put("org.springframework.jms", null);
        restrictedDeps.put("org.springframework.orm", null);
        restrictedDeps.put("org.springframework.oxm", null);
        restrictedDeps.put("org.springframework.transaction", null);
        restrictedDeps.put("org.springframework.web", null);
        restrictedDeps.put("org.springframework.web.servlet", null);
        restrictedDeps.put("org.springframework.web.portlet", null);
        restrictedDeps.put("com.springsource.org.aopalliance", null);
*/
    }

    /**
     * The main entry point for this Maven plugin
     * 
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try ( MrSoaMojoSharedState state = MrSoaMojoSharedState.readState() ) {
            packageService(state);
            packageClient(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // if multiple projects are serially processing, clear out this project's state so it doesn't pollute the next project
        MrSoaMojoSharedState.saveState(null);
    }
    
    private List<Artifact> calculateServiceManifestEntries(MrSoaMojoSharedState state, Consumer<Map<String,String>> serviceManifestEntriesConsumer) throws MojoFailureException, MojoExecutionException {
        Map<String,String> manifestEntries = new LinkedHashMap<>();

        List<String> osgiExports = new ArrayList<String>();
        if (osgi != null && osgi.export != null && !osgi.export.isEmpty())
            Pattern.compile(",").splitAsStream(osgi.export)
                                    .map(e->e.replaceAll("\\s|\r|\n", ""))
                                    .collect(Collectors.toCollection(()->osgiExports));

        String osgiExportProperty = projectProps.getProperty("osgi.export.package");
        if (osgiExportProperty != null && !osgiExportProperty.trim().isEmpty())
            Pattern.compile(",").splitAsStream(osgiExportProperty)
                                    .map(e->e.replaceAll("\\s", ""))
                                    .collect(Collectors.toCollection(()->osgiExports));

        List<String> clientTargetClassesPaths;
        try {
            clientTargetClassesPaths = Files.walk(FileSystems.getDefault().getPath(projectTargetClassesPath))
                                                .filter(path -> path.toFile().isFile())
                                                .map(path -> path.toString().substring(projectTargetClassesPath.length()+1))
                                                .filter(pathStr -> pathStr.matches("clientproxy"+fileSeparatorRegex+".*"))
                                                .collect(toList());
        } catch (IOException e) {
            throw new MojoExecutionException("Error accessing generated client classes", e);
        }

        String exportClientJar = projectProps.getProperty("osgi.export.package.clientjar");
        if (exportClientJar != null && "true".equals(exportClientJar.trim().toLowerCase())) {
            if (clientJar == null || clientJar.includes == null || clientJar.includes.isEmpty())
                throw new MojoFailureException("osgi.export.package.clientjar was specified but no clientJar defined");

            Set<String> clientTargetPackages = new HashSet<String>();
            for (String clientTargetClassPath : clientTargetClassesPaths) {
                String[] clientTargetParts = clientTargetClassPath.split("/");
                if (clientTargetParts.length != 3)
                    throw new MojoExecutionException("Unrecognized file in clientproxy path: " + clientTargetClassPath);
                clientTargetPackages.add(clientTargetParts[0] + "." + clientTargetParts[1]);
            }
            osgiExports.add(clientTargetPackages.stream().collect(joining(",")));

            Map<String,List<String>> includeClassesByPackage = new HashMap<String,List<String>>();
            for (Object includeObj : clientJar.includes) {
                String include = ((String)includeObj).trim();
                if (include.endsWith("/*")) {
                    String pkg = include.substring(0, include.length()-2).replaceAll("/", ".");
                    //osgiExports.add(pkg + ";include:=\"*\"");
                    List<String> includeClasses = includeClassesByPackage.containsKey(pkg) ? includeClassesByPackage.get(pkg) : new ArrayList<String>();
                    if (includeClasses.isEmpty() || !includeClasses.get(includeClasses.size()-1).equals("*"))
                        includeClasses.add("*");
                    includeClassesByPackage.put(pkg, includeClasses);
                } else if (include.endsWith(".*")) {
                    int lastSlashIdx = include.lastIndexOf("/");
                    if (lastSlashIdx == -1)
                        throw new MojoFailureException("Invalid clientJar package for osgi export: " + include);
                    String pkg = include.substring(0, lastSlashIdx).replaceAll("/", ".");
                    String cls = include.substring(lastSlashIdx+1, include.length()-2);
                    //osgiExports.add(pkg + ";include:=\"" + cls + "\"");
                    List<String> includeClasses = includeClassesByPackage.containsKey(pkg) ? includeClassesByPackage.get(pkg) : new ArrayList<String>();
                    if (includeClasses.isEmpty() || !includeClasses.get(includeClasses.size()-1).equals("*"))
                        includeClasses.add(cls);
                    includeClassesByPackage.put(pkg, includeClasses);
                } else if (include.endsWith("*")) {
                    int lastSlashIdx = include.lastIndexOf("/");
                    if (lastSlashIdx == -1)
                        throw new MojoFailureException("Invalid clientJar package for osgi export: " + include);
                    String pkg = include.substring(0, lastSlashIdx).replaceAll("/", ".");
                    String cls = include.substring(lastSlashIdx+1);
                    //osgiExports.add(pkg + ";include:=\"" + cls + "\"");
                    List<String> includeClasses = includeClassesByPackage.containsKey(pkg) ? includeClassesByPackage.get(pkg) : new ArrayList<String>();
                    if (includeClasses.isEmpty() || !includeClasses.get(includeClasses.size()-1).equals("*"))
                        includeClasses.add(cls);
                    includeClassesByPackage.put(pkg, includeClasses);
                }
            }

            Iterator<Entry<String,List<String>>> includePackagesAndClassesIt = includeClassesByPackage.entrySet().iterator();
            while (includePackagesAndClassesIt.hasNext()) {
                Entry<String,List<String>> includePackagesAndClassesEntry = includePackagesAndClassesIt.next();
                String pkg = includePackagesAndClassesEntry.getKey();
                List<String> clss = includePackagesAndClassesEntry.getValue();
                StringBuffer includeString = new StringBuffer();
                Iterator<String> clssIt = clss.iterator();
                while (clssIt.hasNext()) {
                    includeString.append(clssIt.next());
                    if (clssIt.hasNext())
                        includeString.append(",");
                }
                osgiExports.add(pkg + ";include:=\"" + includeString.toString() + "\"");
            }
        }

        if (!osgiExports.isEmpty())
            manifestEntries.put("Export-Package", osgiExports.stream().collect(joining(",")));

        String requireBundlesString = "mrsoa-kernel;bundle-version=3.0";
        String requirePropStringRoot = projectProps.getProperty("osgi.require.bundle.parent");
        String requirePropString = projectProps.getProperty("osgi.require.bundle");
        requireBundlesString = requireBundlesString +
                               (requirePropStringRoot != null && !requirePropStringRoot.trim().isEmpty() ? "," + requirePropStringRoot.replaceAll("\\s", "") : "") +
                               (requirePropString     != null && !requirePropString.trim().isEmpty()     ? "," + requirePropString.replaceAll("\\s", "")     : "");

        manifestEntries.put("Require-Bundle", requireBundlesString);

        List<Artifact> dependencyArtifacts = new ArrayList<>();
        List<String> bundleClassPath = new ArrayList<>();
        bundleClassPath.add(".");
        for (Dependency dep: projectDependencies) {
            if ("compile".equals(dep.getScope()) || "runtime".equals(dep.getScope())) {
                if (restrictedDeps.containsKey(dep.getArtifactId()))
                    throw new MojoFailureException("Maven project dependency '" + dep.getArtifactId() + "' may not be bundled in service jar (use scope other than 'compile')");
                Artifact artifact = projectDependencyArtifacts.stream()
                                                                  .filter(a -> a.getGroupId().equals(dep.getGroupId()) &&
                                                                               a.getArtifactId().equals(dep.getArtifactId()) && (
                                                                                   a.getVersion().equals(dep.getVersion()) ||
                                                                                   dep.getVersion().equals(a.getVersionRange().toString()) ))
                                                                  .findFirst().orElse(null);
                if (artifact != null) {
                    bundleClassPath.add(artifact.getFile().getName());
                    dependencyArtifacts.add(artifact);
                }
            }
        }

        manifestEntries.put("Bundle-Classpath", bundleClassPath.stream().collect(joining(",")));

        manifestEntries.put("Bundle-SymbolicName", project.getGroupId() + "." + project.getArtifactId());
        manifestEntries.put("Bundle-Version", project.getVersion());
        manifestEntries.put("Bundle-Category", "mrsoa-service-r1");
        manifestEntries.put("Bundle-Activator", OSGI_ACTIVATOR_CLASSNAME);
        
        serviceManifestEntriesConsumer.accept(manifestEntries);
        return dependencyArtifacts;
    }

    private void packageService(MrSoaMojoSharedState state) throws ArchiverException, ManifestException, IOException, DependencyResolutionRequiredException, MojoFailureException, MojoExecutionException {
        /* Generate service JAR */
        File projectTargetClassesFile = new File(projectTargetClassesPath);
        
        serviceJarArchiver.addDirectory(projectTargetClassesFile, new String[] {"**/**"}, null);

        String serviceJarName = finalName + ".jar";
        File serviceJarFile = new File(projectTargetPath, serviceJarName);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(serviceJarArchiver);
        archiver.setOutputFile(serviceJarFile);
        
        List<Artifact> dependencyArtifacts = calculateServiceManifestEntries(state, entries -> serviceArchiveConfig.addManifestEntries(entries) );

        for (Artifact dependencyArtifact : dependencyArtifacts)
            serviceJarArchiver.addFile(dependencyArtifact.getFile(), dependencyArtifact.getFile().getName());

        archiver.createArchive(session, project, serviceArchiveConfig);

        project.getArtifact().setFile(serviceJarFile);
    }

    private void packageClient(MrSoaMojoSharedState state) throws ArchiverException, ManifestException, IOException, DependencyResolutionRequiredException {
        /* Generate client JAR */
        File projectJavaSourcesFile = new File(projectCompileSrcRoots.get(0));
        File projectTargetClassesFile = new File(projectTargetClassesPath);
        File projectTargetGeneratedSourcesFile = state.generatedSourcesFile;

        Supplier<Stream<String>> clientUserIncludePaths = () ->
                clientJar == null ?
                    Stream.empty() :
                    clientJar.includes.stream().map( includePath -> includePath.replaceAll(fileSeparatorRegex, fileSeparatorReplaceRegex) );

        Supplier<Stream<String>> generatedClientFileIncludePatterns = () ->
                state.generatedClientFiles.stream().map( gcf -> gcf + ".*" );
        
        String[] projectTargetClassesIncludes = Stream.concat(clientUserIncludePaths.get(), generatedClientFileIncludePatterns.get()).toArray(String[]::new);
        clientJarArchiver.addDirectory(projectTargetClassesFile, projectTargetClassesIncludes, null);

        String[] projectTargetGeneratedSourcesIncludes = generatedClientFileIncludePatterns.get().toArray(String[]::new);
        clientJarArchiver.addDirectory(projectTargetGeneratedSourcesFile, projectTargetGeneratedSourcesIncludes, null);

        String[] projectJavaSourcesIncludes = clientUserIncludePaths.get().toArray(String[]::new);
        clientJarArchiver.addDirectory(projectJavaSourcesFile, projectJavaSourcesIncludes, null);
        
        String clientJarName = finalName + "-client.jar";
        File clientJarFile = new File(projectTargetPath, clientJarName);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(clientJarArchiver);
        archiver.setOutputFile(clientJarFile);
        
        archiver.createArchive(session, project, clientArchiveConfig);

        projectHelper.attachArtifact(project, "jar", "client", clientJarFile);
    }
}
