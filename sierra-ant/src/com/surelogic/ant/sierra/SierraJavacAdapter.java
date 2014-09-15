package com.surelogic.ant.sierra;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.StringUtils;

import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.remote.AbstractLocalSLJob;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.sierra.tool.IToolExtension;
import com.surelogic.sierra.tool.IToolFactory;
import com.surelogic.sierra.tool.SierraToolConstants;
import com.surelogic.sierra.tool.ToolUtil;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.ToolExtension;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;
import com.surelogic.sierra.tool.targets.JarTarget;

public class SierraJavacAdapter extends DefaultCompilerAdapter {
    Path sourcepath = null;
    final SierraScan scan;

    public SierraJavacAdapter(SierraScan sierraScan) {
        scan = sierraScan;
    }

    public boolean execute() throws BuildException {
        /*
         * for(Object key : System.getProperties().keySet()) {
         * System.out.println("Key: "+key); }
         */
        if (false) {
            checkClassPath("sun.boot.class.path");
            checkClassPath("java.class.path");
        }
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
        return true;
    }

    private void checkClassPath(String key) {
        StringTokenizer st = new StringTokenizer(System.getProperty(key),
                File.pathSeparator);
        while (st.hasMoreTokens()) {
            System.out.println(key + ": " + st.nextToken());
        }
    }

    private Config createConfig() throws IOException {
        Config config = new Config();
        config.setProject(scan.getProjectName());
        setupConfig(config, false);
        logAndAddFilesToCompile(config);

        if (verbose) {
            System.out.println("verbose = " + verbose);
        }
        config.setVerbose(verbose);
        setMemorySize(config);
        config.setJavaVendor(System.getProperty("java.vendor"));
        config.setJavaVersion(System.getProperty("java.version"));

        if (scan.getHome() == null) {
            throw new BuildException("No value for home");
        }
        // C:/work/workspace/sierra-ant
        final String libHome = scan.getHome() + "/lib/";
        if (!new File(libHome).exists()) {
            throw new BuildException("No lib subdirectory under " + libHome);
        }
        System.setProperty(ToolUtil.TOOLS_PATH_PROP_NAME, libHome);
        final String toolsDir = libHome + "tools/";
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
        config.setToolsDirectory(new File(toolsDir + "reckoner"));
        config.putPluginDir(AbstractLocalSLJob.COMMON_PLUGIN_ID, libHome
                + "common.jar");
        config.putPluginDir(SierraToolConstants.MESSAGE_PLUGIN_ID, libHome
                + "sierra-message.jar");
        config.putPluginDir(SierraToolConstants.PMD_PLUGIN_ID, toolsDir + "pmd");
        config.putPluginDir(SierraToolConstants.FB_PLUGIN_ID, toolsDir
                + "findbugs");
        config.putPluginDir(SierraToolConstants.TOOL_PLUGIN_ID, libHome
                + "sierra-tool.jar");
        config.putPluginDir(SierraToolConstants.JUNIT4_PLUGIN_ID, libHome
                + "junit");
        // System.out.println("Using source level "+scan.getSource());
        config.setSourceLevel(scan.getSource());

        File scanDocument = scan.getScanFile();
        config.setScanDocument(scanDocument);
        config.setLogPath(scan.getDocument() + AbstractRemoteSLJob.LOG_SUFFIX);
        return config;
    }

    private void setMemorySize(Config config) {
        int max = parseMemorySize(scan.getMemoryMaximumSize());
        int init = parseMemorySize(scan.getMemoryInitialSize());
        config.setMemorySize(max > init ? max : init);
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

    private void addPath(Config config, Type type, Path path) {
        for (String elt : path.list()) {
            File f = new File(elt);
            if (f.exists()) {
                // System.out.println(type+": "+elt);
                if (f.isDirectory()) {
                    config.addTarget(new FullDirectoryTarget(type, f.toURI()));
                } else {
                    config.addTarget(new JarTarget(type, f.toURI()));
                }
            }
        }
    }

    /**
     * Originally based on
     * DefaultCompilerAdapter.setupJavacCommandlineSwitches()
     */
    protected Config setupConfig(Config cmd, boolean useDebugLevel) {
        Path classpath = getCompileClasspath();

        // For -sourcepath, use the "sourcepath" value if present.
        // Otherwise default to the "srcdir" value.
        Path sourcepath;
        if (compileSourcepath != null) {
            sourcepath = compileSourcepath;
        } else {
            sourcepath = src;
        }

        /*
         * if (memoryMaximumSize != null) { if (!attributes.isForkedJavac()) {
         * attributes.log("Since fork is false, ignoring " + "memoryMaximumSize
         * setting.", Project.MSG_WARN); } else {
         * cmd.createArgument().setValue(memoryParameterPrefix + "mx" +
         * memoryMaximumSize); } }
         */

        if (destDir != null) {
            cmd.addTarget(new FullDirectoryTarget(Type.BINARY, destDir.toURI()));
        }

        addPath(cmd, Type.AUX, classpath);

        // If the buildfile specifies sourcepath="", then don't
        // output any sourcepath.
        if (sourcepath.size() > 0) {
            // addPath(cmd, Type.SOURCE, sourcepath);
            this.sourcepath = sourcepath;
        }

        /*
         * Path bp = getBootClassPath(); if (bp.size() > 0) { addPath(cmd,
         * Type.AUX, bp); }
         */

        /*
         * if (verbose) { cmd.createArgument().setValue("-verbose"); }
         */

        return cmd;
    }

    /**
     * Based on DefaultCompilerAdapter.logAndAddFilesToCompile()
     */
    protected void logAndAddFilesToCompile(Config config) {
        attributes.log("Compilation for " + config.getProject(),
                Project.MSG_VERBOSE);

        StringBuffer niceSourceList = new StringBuffer("File");
        if (compileList.length != 1) {
            niceSourceList.append('s');
        }
        niceSourceList.append(" to be compiled:");

        niceSourceList.append(StringUtils.LINE_SEP);

        for (int i = 0; i < compileList.length; i++) {
            String arg = compileList[i].getAbsolutePath();
            config.addTarget(new FileTarget(Type.SOURCE, new File(arg).toURI(),
                    findSrcDir(arg)));
            niceSourceList.append("    ");
            niceSourceList.append(arg);
            niceSourceList.append(StringUtils.LINE_SEP);
        }
        /*
         *
         * if (attributes.getSourcepath() != null) { addPath(config,
         * Type.SOURCE, attributes.getSourcepath()); } else { addPath(config,
         * Type.SOURCE, attributes.getSrcdir()); } addPath(config, Type.AUX,
         * attributes.getClasspath());
         */

        attributes.log(niceSourceList.toString(), Project.MSG_VERBOSE);
    }

    private URI findSrcDir(String arg) {
        for (String src : sourcepath.list()) {
            if (arg.startsWith(src)) {
                return new File(src).toURI();
            }
        }
        return null;
    }
}
