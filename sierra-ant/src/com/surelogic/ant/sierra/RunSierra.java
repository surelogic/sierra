package com.surelogic.ant.sierra;

import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.combineLists;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getBytecodePackagePatterns;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getBytecodeSourceFolders;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getExcludedPackagePatterns;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getExcludedSourceFolders;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import com.surelogic.common.SLUtility;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.AbstractLocalSLJob;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.tool.SureLogicToolsPropertiesUtility;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.ToolExtension;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;
import com.surelogic.sierra.tool.targets.JarTarget;

public class RunSierra extends Task {
    private static final String LIB_PATH = "lib";
    private static final String TOOLS_PATH = "lib/tools";
    private String name;
    private Path sources;
    private Path classes;
    private Path classpath;
    private File properties;
    String[] excludedPackages = null;
    private File outputDir;
    private File toolHome;
    private boolean verbose;
    private String memoryMaximumSize;
    private String memoryInitialSize;
    private String sourceLevel;

    public RunSierra() {
    }

    @Override
    public void execute() throws BuildException {
        try {
            Config config = createConfig();
            SLStatus status = ToolUtil.scan(System.out, config,
                    new NullSLProgressMonitor(), true);
            if (status.getException() != null) {
                throw status.getException();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new BuildException("Exception while scanning", t);
        }
    }

    private Config createConfig() {
        Config config = new Config();
        config.setProject(name);
        excludedPackages = loadProperties(config);
        if (sources != null) {
            addPath(config, Type.SOURCE, sources);
        }
        if (classes != null) {
            addPath(config, Type.BINARY, classes);
        }
        if (classpath != null) {
            addPath(config, Type.AUX, classpath);
        }

        config.setVerbose(verbose);

        config.setJavaVendor(System.getProperty("java.vendor"));
        config.setJavaVersion(System.getProperty("java.version"));
        int max = parseMemorySize(getMemoryMaximumSize());
        int init = parseMemorySize(getMemoryInitialSize());
        config.setMemorySize(max > init ? max : init);

        if (toolHome == null) {
            throw new BuildException("No value for home");
        }
        final File libHome = new File(toolHome, LIB_PATH);
        if (!libHome.exists()) {
            throw new BuildException("No lib subdirectory under " + libHome);
        }
        System.setProperty(ToolUtil.TOOLS_PATH_PROP_NAME,
                libHome.getAbsolutePath());
        final File toolsDir = new File(toolHome, TOOLS_PATH);
        for (IToolFactory f : ToolUtil.findToolFactories()) {
            for (final IToolExtension t : f.getExtensions()) {
                /*
                 * if (t.isCore()) { // Implied by the above continue; }
                 */
                final ToolExtension ext = new ToolExtension();
                ext.setTool(f.getId());
                ext.setId(t.getId());
                ext.setVersion(t.getVersion());
                config.addExtension(ext);
            }
        }
        config.setToolsDirectory(new File(toolsDir, "reckoner"));
        config.putPluginDir(AbstractLocalSLJob.COMMON_PLUGIN_ID, new File(
                libHome, "common.jar").getAbsolutePath());
        config.putPluginDir(SierraToolConstants.MESSAGE_PLUGIN_ID, new File(
                libHome, "sierra-message.jar").getAbsolutePath());
        config.putPluginDir(SierraToolConstants.PMD_PLUGIN_ID, new File(
                toolsDir, "pmd").getAbsolutePath());
        config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID, new File(
                toolsDir, "findbugs").getAbsolutePath());
        config.putPluginDir(SierraToolConstants.TOOL_PLUGIN_ID, new File(
                libHome, "sierra-tool.jar").getAbsolutePath());
        config.putPluginDir(SierraToolConstants.JUNIT4_PLUGIN_ID, new File(
                libHome, "junit").getAbsolutePath());
        // System.out.println("Using source level "+scan.getSource());
        config.setSourceLevel(sourceLevel);

        config.setScanDocument(new File(outputDir, name
                + SierraToolConstants.PARSED_FILE_SUFFIX).getAbsoluteFile());
        config.setLogPath(new File(outputDir, name
                + AbstractRemoteSLJob.LOG_SUFFIX).getAbsolutePath());
        return config;
    }

    private void addPath(Config config, Type type, Path path) {
        for (String elt : path.list()) {
            File f = new File(elt);
            if (f.exists()) {
                if (f.isDirectory()) {
                    if (excludedPackages == null) {
                        config.addTarget(new FullDirectoryTarget(type, f
                                .toURI()));
                    } else {
                        config.addTarget(new FilteredDirectoryTarget(type, f
                                .toURI(), null, excludedPackages));
                    }
                } else {
                    config.addTarget(new JarTarget(type, f.toURI()));
                }
            }
        }
    }

    protected String[] loadProperties(Config cfg) {
        File propFile = getProperties();
        if (propFile != null) {
            final Properties props = SureLogicToolsPropertiesUtility
                    .readFileOrNull(getProperties());
            final String[] excludedFolders = makeAbsolute(getExcludedSourceFolders(props));
            log("Excluded Folders: " + Arrays.toString(excludedFolders));
            final String[] bytecodeFolders = makeAbsolute(getBytecodeSourceFolders(props));
            log("Bytecode Folders: " + Arrays.toString(bytecodeFolders));
            final String[] excludedPackages = convertPkgsToSierraStyle(getExcludedPackagePatterns(props));
            log("Excluded Packages: " + Arrays.toString(excludedPackages));
            final String[] bytecodePackages = convertPkgsToSierraStyle(getBytecodePackagePatterns(props));
            log("Bytecode Packages: " + Arrays.toString(bytecodePackages));
            final String[] combinedPackages = combineLists(excludedPackages,
                    bytecodePackages);
            if (props != null) {
                log(String.format("Loading properties file at %s.",
                        getProperties()));
                cfg.initFromToolsProps(props, excludedFolders, excludedPackages);
            } else {
                log("No properties file loaded.");
            }
            return combinedPackages;
        }

        return new String[] {};
    }

    private static String[] convertPkgsToSierraStyle(String[] pkgs) {
        if (pkgs == null || pkgs.length == 0) {
            return SLUtility.EMPTY_STRING_ARRAY;
        }
        final String[] paths = new String[pkgs.length];
        int i = 0;
        for (String p : pkgs) {
            paths[i] = p.replace('.', '/').replaceAll("\\*", "**");
            i++;
        }
        return paths;
    }

    private static String[] makeAbsolute(String[] excludedPaths) {
        String[] rv = new String[excludedPaths.length];
        for (int i = 0; i < rv.length; i++) {
            File f = new File(excludedPaths[i]);
            rv[i] = f.getAbsolutePath();
        }
        return rv;
    }

    private int parseMemorySize(String memSize) {
        if (memSize != null && !"".equals(memSize)) {
            int last = memSize.length() - 1;
            char lastChar = memSize.charAt(last);
            int size, mb = 1024;
            switch (lastChar) {
            case 'm':
            case 'M':
                mb = Integer.parseInt(memSize.substring(0, last));
                break;
            case 'g':
            case 'G':
                size = Integer.parseInt(memSize.substring(0, last));
                mb = size * 1024;
                break;
            case 'k':
            case 'K':
                size = Integer.parseInt(memSize.substring(0, last));
                mb = (int) Math.ceil(size / 1024.0);
                break;
            default:
                // in bytes
                size = Integer.parseInt(memSize);
                mb = (int) Math.ceil(size / (1024 * 1024.0));
            }
            return mb;
        }
        return 1024;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path createSources() {
        if (sources == null) {
            sources = new Path(getProject());
        }
        return sources.createPath();
    }

    public Path createClasses() {
        if (classes == null) {
            classes = new Path(getProject());
        }
        return classes.createPath();
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    public File getProperties() {
        return properties;
    }

    public void setProperties(File properties) {
        this.properties = properties;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public File getToolHome() {
        return toolHome;
    }

    public void setToolHome(File toolHome) {
        this.toolHome = toolHome;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getMemoryMaximumSize() {
        return memoryMaximumSize;
    }

    public void setMemoryMaximumSize(String memoryMaximumSize) {
        this.memoryMaximumSize = memoryMaximumSize;
    }

    public String getMemoryInitialSize() {
        return memoryInitialSize;
    }

    public void setMemoryInitialSize(String memoryInitialSize) {
        this.memoryInitialSize = memoryInitialSize;
    }

    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

}
