package com.surelogic.sierra.ant;

import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.combineLists;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getBytecodePackagePatterns;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getExcludedPackagePatterns;
import static com.surelogic.common.tool.SureLogicToolsPropertiesUtility.getExcludedSourceFolders;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.StringUtils;

import com.surelogic.common.FileUtility;
import com.surelogic.common.FileUtility.TempFileFilter;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
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
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget.Type;
import com.surelogic.sierra.tool.targets.JarTarget;

public class SierraJavacAdapter extends DefaultCompilerAdapter {
  Path sourcepath = null;
  String[] excludedPackages = null;
  final SierraScan scan;

  public SierraJavacAdapter(SierraScan sierraScan) {
    scan = sierraScan;
  }

  public boolean execute() throws BuildException {
    try {
      System.out.println("Project to scan w/Sierra = " + scan.getProjectName());

      // temp output location for scan
      final TempFileFilter scanDirFileFilter = new TempFileFilter("sierraAnt", ".scandir");
      final File tempDir = scanDirFileFilter.createTempFolder();

      // checkClassPath("sun.boot.class.path");
      // checkClassPath("java.class.path");

      /*
       * Setup config to run the scan
       */
      final Config config = new Config();
      config.setProject(scan.getProjectName());
      excludedPackages = loadProperties(config);
      setupConfig(config, false);
      logAndAddFilesToCompile(config);

      if (verbose) {
        System.out.println("verbose = " + verbose);
      }
      config.setVerbose(verbose);
      setMemorySize(config);
      config.setJavaVendor(System.getProperty("java.vendor"));
      config.setJavaVersion(System.getProperty("java.version"));

      final File sierraAntHome = scan.getSierraAntHomeAsFile();
      System.setProperty(ToolUtil.TOOLS_PATH_PROP_NAME, sierraAntHome.getAbsolutePath());

      for (IToolFactory f : ToolUtil.findToolFactories()) {
        for (final IToolExtension t : f.getExtensions()) {
          final ToolExtension ext = new ToolExtension();
          ext.setTool(f.getId());
          ext.setId(t.getId());
          ext.setVersion(t.getVersion());
          config.addExtension(ext);
        }
      }
      addPluginToConfig(sierraAntHome, AbstractLocalSLJob.COMMON_PLUGIN_ID, false, config);
      addPluginToConfig(sierraAntHome, SierraToolConstants.MESSAGE_PLUGIN_ID, false, config);
      addPluginToConfig(sierraAntHome, SierraToolConstants.PMD_PLUGIN_ID, true, config);
      addPluginToConfig(sierraAntHome, SierraToolConstants.FB_PLUGIN_ID, true, config);
      addPluginToConfig(sierraAntHome, SierraToolConstants.TOOL_PLUGIN_ID, false, config);

      config.setSourceLevel(scan.getSource());

      final String scanDocumentName = config.getProject() + SierraToolConstants.PARSED_FILE_SUFFIX;
      final String logFileName = config.getProject() + "." + ToolUtil.getTimeStamp() + AbstractRemoteSLJob.LOG_SUFFIX;
      final String finalZipName = config.getProject() + "." + ToolUtil.getTimeStamp() + SierraToolConstants.SIERRA_SCAN_TASK_SUFFIX;

      File scanDocument = new File(tempDir, scanDocumentName);
      config.setScanDocument(scanDocument);
      config.setLogPath(new File(tempDir, logFileName).getAbsolutePath());

      // check if any tools were found (for less cryptic output)
      if (ToolUtil.getNumTools(config) < 1)
        throw new BuildException("No Sierra tools found (e.g., PMD or FindBugs)...this is a bug");

      // surelogic-tools.properties file
      final File surelogicToolsProperties = scan.getSurelogicToolsPropertiesAsFile();
      if (surelogicToolsProperties != null)
        System.out.println("Using properties         = " + surelogicToolsProperties.getAbsolutePath());

      final String scanOutputDirMsg;
      if (scan.getSierraScanDir() == null)
        scanOutputDirMsg = ".";
      else
        scanOutputDirMsg = scan.getSierraScanDirAsFile().getAbsolutePath();
      System.out.println("Scan output directory    = " + scanOutputDirMsg);

      final SLStatus status = ToolUtil.scan(System.out, config, new NullSLProgressMonitor(), true);
      if (status.getException() != null) {
        throw status.getException();
      }

      final File zipFile = new File(scan.getSierraScanDirAsFile(), finalZipName);

      FileUtility.zipDir(tempDir, zipFile);
      if (!FileUtility.recursiveDelete(tempDir)) {
        System.out.println("Error unable to delete temp dir " + tempDir.getAbsolutePath());
      }

    } catch (Throwable t) {
      t.printStackTrace();
      throw new BuildException("Exception while scanning " + scan.getProjectName(), t);
    }
    return true;
  }

  @SuppressWarnings("unused")
  private void checkClassPath(String key) {
    StringTokenizer st = new StringTokenizer(System.getProperty(key), File.pathSeparator);
    while (st.hasMoreTokens()) {
      System.out.println(key + ": " + st.nextToken());
    }
  }

  /**
   * Helper method to add a plugin (tool or normal) to the passed config.
   * 
   * @param sierraAntHome
   *          location of the Sierra Ant task code
   * @param id
   *          a plugin identifier.
   * @param isTool
   *          {@code true} if the plugin is an analysis tool, {@code false}
   *          otherwise
   * @param config
   *          the mutable configuration to add the information to
   */
  private void addPluginToConfig(File sierraAntHome, String id, boolean isTool, Config config) {
    config.putPluginDir(id, getAntPluginDirectory(sierraAntHome, id, isTool).getAbsolutePath());
  }

  /**
   * Helper to determine the directory of a fake Eclipse plugin in the Ant
   * directory structure. By convention we use the identifier as the directory
   * name.
   * 
   * @param sierraAntHome
   *          location of the Sierra Ant task code
   * @param pluginId
   *          a plugin identifier.
   * @param isTool
   *          {@code true} if the plugin is an analysis tool, {@code false}
   *          otherwise
   * @return a directory.
   * 
   * @throws IllegalArgumentException
   *           if no path can be found for the passed pluginId
   */
  File getAntPluginDirectory(File sierraAntHome, final String pluginId, boolean isTool) {
    final String subdir = isTool ? FileUtility.TOOLS_PATH_FRAGMENT : "lib";
    final File result = new File(new File(sierraAntHome, subdir), pluginId);
    if (result.exists())
      return result;
    else {
      throw new IllegalArgumentException(I18N.err(340, pluginId, result.getAbsolutePath()));
    }
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
          if (excludedPackages != null) {
            config.addTarget(new FilteredDirectoryTarget(type, f.toURI(), null, excludedPackages));
          } else {
            config.addTarget(new FullDirectoryTarget(type, f.toURI()));
          }
        } else {
          config.addTarget(new JarTarget(type, f.toURI()));
        }
      }
    }
  }

  private static String[] makeAbsolute(String[] excludedPaths) {
    String[] rv = new String[excludedPaths.length];
    for (int i = 0; i < rv.length; i++) {
      File f = new File(excludedPaths[i]);
      rv[i] = f.getAbsolutePath();
    }
    return rv;
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

  protected String[] loadProperties(Config cfg) {
    final File propFile = scan.getSurelogicToolsPropertiesAsFile();
    if (propFile != null) {
      final Properties props = SureLogicToolsPropertiesUtility.readFileOrNull(propFile);
      final String[] excludedFolders = makeAbsolute(getExcludedSourceFolders(props));
      final String[] excludedPackages = convertPkgsToSierraStyle(getExcludedPackagePatterns(props));
      final String[] bytecodePackages = convertPkgsToSierraStyle(getBytecodePackagePatterns(props));
      final String[] combinedPackages = combineLists(excludedPackages, bytecodePackages);
      if (props != null) {
        cfg.initFromToolsProps(props, excludedFolders, excludedPackages);
      }
      return combinedPackages;
    }

    return new String[] {};
  }

  /**
   * Originally based on DefaultCompilerAdapter.setupJavacCommandlineSwitches()
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
      if (excludedPackages == null) {
        cmd.addTarget(new FullDirectoryTarget(Type.BINARY, destDir.toURI()));
      } else {
        cmd.addTarget(new FilteredDirectoryTarget(Type.BINARY, destDir.toURI(), null, excludedPackages));
      }

    }

    addPath(cmd, Type.AUX, classpath);

    // If the buildfile specifies sourcepath="", then don't
    // output any sourcepath.
    if (sourcepath.size() > 0) {
      // addPath(cmd, Type.SOURCE, sourcepath);
      this.sourcepath = sourcepath;
    }

    /*
     * Path bp = getBootClassPath(); if (bp.size() > 0) { addPath(cmd, Type.AUX,
     * bp); }
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
    attributes.log("Compilation for " + config.getProject(), Project.MSG_VERBOSE);

    StringBuffer niceSourceList = new StringBuffer("File");
    if (compileList.length != 1) {
      niceSourceList.append('s');
    }
    niceSourceList.append(" to be compiled:");

    niceSourceList.append(StringUtils.LINE_SEP);

    for (int i = 0; i < compileList.length; i++) {
      String arg = compileList[i].getAbsolutePath();
      config.addTarget(new FileTarget(Type.SOURCE, new File(arg).toURI(), findSrcDir(arg)));
      niceSourceList.append("    ");
      niceSourceList.append(arg);
      niceSourceList.append(StringUtils.LINE_SEP);
    }
    /*
     * 
     * if (attributes.getSourcepath() != null) { addPath(config, Type.SOURCE,
     * attributes.getSourcepath()); } else { addPath(config, Type.SOURCE,
     * attributes.getSrcdir()); } addPath(config, Type.AUX,
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
