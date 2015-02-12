package com.surelogic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import com.surelogic.ant.sierra.RunSierra;
import com.surelogic.common.FileUtility;

/**
 * Goal which touches a timestamp file.
 *
 *
 */
@Mojo(name = "scan")
@Execute(phase = LifecyclePhase.COMPILE)
public class ScanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> pluginRepos;

    /**
     * The project's remote repositories to use for the resolution of plugins
     * and their dependencies.
     *
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Component
    private ProjectDependenciesResolver resolver;

    /**
     * The current repository/network configuration of Maven.
     *
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project}")
    private org.apache.maven.project.MavenProject mavenProject;
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    @Component
    private RepositorySystem repoSystem;

    @Parameter(property = "properties", required = false)
    private File properties;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = false)
    private File outputDirectory;
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "binDir", required = false)
    private File binDirectory;
    @Parameter(defaultValue = "${project.build.testOutputDirectory}", property = "testBinDir", required = false)
    private File testBinDirectory;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "srcDir", required = false)
    private File sourceDirectory;
    @Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "testSrcDir", required = false)
    private File testSourceDirectory;

    @Parameter(defaultValue = "${project.artifactId}", property = "project", required = false)
    private String projectName;
    @Parameter(property = "sourceLevel", required = false)
    private String sourceLevel;

    @Parameter(property = "toolHome", required = false)
    private File toolHome;
    @Parameter(property = "memoryMaximumSize", required = false)
    private String maxMem;
    @Parameter(property = "memoryInitialSize", required = false)
    private String initMem;
    @Parameter(property = "verbose", required = false)
    private boolean verbose;

    @Parameter(defaultValue = "${plugin.version}", readonly = true)
    private String version;

    @Component
    MavenProject project;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            ".yyyy.MM.dd-'at'-HH.mm.ss.SSS");

    @Override
    public void execute() throws MojoExecutionException {
        File toDelete = null;
        try {
            String sourceLevel = calculateSourceLevel();
            RunSierra rs = new RunSierra();
            rs.setProperties(properties);
            rs.setOutputDir(outputDirectory);
            if (toolHome == null || !toolHome.exists()) {
                File tmp = File.createTempFile("surelogic", "tools");
                tmp.delete();
                tmp.mkdir();
                ArtifactRequest runtimeRequest = new ArtifactRequest();
                runtimeRequest.setArtifact(new DefaultArtifact(
                        "com.surelogic:sierra-ant-archive:zip:" + version));
                runtimeRequest.setRepositories(pluginRepos);
                ArtifactResult runtimeResult = repoSystem.resolveArtifact(
                        repoSession, runtimeRequest);
                File archive = runtimeResult.getArtifact().getFile();
                FileUtility.unzipFile(archive, tmp);
                if (toolHome != null) {
                    File from = new File(tmp, "sierra-ant");
                    if (!from.renameTo(toolHome)) {
                        FileUtility.recursiveCopy(from, toolHome);
                    }
                    FileUtility.recursiveDelete(tmp);
                } else {
                    toolHome = new File(tmp, "sierra-ant");
                    toDelete = tmp;
                }
            }
            rs.setToolHome(toolHome);
            Path bin = rs.createClasses();
            bin.createPathElement().setLocation(binDirectory);
            bin.createPathElement().setLocation(testBinDirectory);
            Path src = rs.createSources();
            src.createPathElement().setLocation(sourceDirectory);
            src.createPathElement().setLocation(testSourceDirectory);
            rs.setName(projectName);
            rs.setSourceLevel(sourceLevel);
            rs.setMemoryInitialSize(initMem);
            rs.setMemoryMaximumSize(maxMem);
            rs.setVerbose(verbose);
            DefaultDependencyResolutionRequest depRequest = new DefaultDependencyResolutionRequest(
                    mavenProject, repoSession);
            DependencyResolutionResult depResult;
            depResult = resolver.resolve(depRequest);

            Path cp = rs.createClasspath();
            List<Dependency> dependencies = depResult.getDependencies();
            for (Dependency d : dependencies) {
                Artifact a = d.getArtifact();
                if (a.getFile() == null) {
                    ArtifactRequest request = new ArtifactRequest();
                    request.setArtifact(a);
                    request.setRepositories(remoteRepos);
                    ArtifactResult result = repoSystem.resolveArtifact(
                            repoSession, request);
                    Artifact resultArtifact = result.getArtifact();
                    cp.createPathElement()
                            .setLocation(resultArtifact.getFile());
                } else {
                    cp.createPathElement().setLocation(a.getFile());
                }
            }

            rs.execute();

            if (toDelete != null) {
                FileUtility.recursiveDelete(toDelete);
            }

        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Problem encountered while running task", e);
        }

    }

    private String calculateSourceLevel() {
        if (sourceLevel != null) {
            return sourceLevel;
        }
        String level = "1.5";
        List<Plugin> buildPlugins = project.getBuildPlugins();
        for (Plugin p : buildPlugins) {
            if ("org.apache.maven.plugins:maven-compiler-plugin".equals(p
                    .getKey())) {
                Xpp3Dom dom = (Xpp3Dom) p.getConfiguration();
                if (dom != null) {
                    Xpp3Dom child = dom.getChild("source");
                    if (child != null) {
                        level = child.getValue();
                    }
                }
            }
        }
        return level;
    }

}
