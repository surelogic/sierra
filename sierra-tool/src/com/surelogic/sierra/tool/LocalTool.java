package com.surelogic.sierra.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.surelogic.common.jobs.remote.AbstractLocalSLJob;
import com.surelogic.common.jobs.remote.AbstractRemoteSLJob;
import com.surelogic.common.jobs.remote.ConfigHelper;
import com.surelogic.common.jobs.remote.RemoteSLJobException;
import com.surelogic.common.jobs.remote.TestCode;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.KeyValuePair;
import com.surelogic.sierra.tool.targets.FileTarget;
import com.surelogic.sierra.tool.targets.FilteredDirectoryTarget;
import com.surelogic.sierra.tool.targets.FullDirectoryTarget;
import com.surelogic.sierra.tool.targets.IToolTarget;
import com.surelogic.sierra.tool.targets.JarTarget;

final class LocalTool extends AbstractLocalSLJob<Config>implements IToolInstance {
  private static final JAXBContext ctx = createContext();

  private static final Marshaller marshaller = createMarshaller(ctx);

  private static final IToolFactory factory = new DummyToolFactory("Local", "1.0", "Local",
      "Local tool for running other tools in another JVM");

  private static JAXBContext createContext() {
    try {
      return JAXBContext.newInstance(Config.class, FileTarget.class, JarTarget.class, FullDirectoryTarget.class, KeyValuePair.class,
          FilteredDirectoryTarget.class);
    } catch (JAXBException e) {
      LOG.log(Level.SEVERE, "Couldn't create JAXB context", e);
      return null;
    }
  }

  private static Marshaller createMarshaller(final JAXBContext ctx) {
    if (ctx == null) {
      LOG.severe("No JAXB context to create marshaller with");
      return null;
    }
    try {
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      return marshaller;
    } catch (JAXBException e) {
      LOG.log(Level.SEVERE, "Couldn't create JAXB marshaller", e);
      return null;
    }
  }

  public LocalTool(final Config config) {
    // super("Sierra tool",,
    // TestCode.getTestCode(config.getTestCode()),
    // config.getMemorySize(), /*true ||*/ config.isVerbose());
    super("Sierra tool", ToolUtil.getNumTools(config), config);
  }

  @Override
  protected RemoteSLJobException newException(final int number, final Object... args) {
    throw new ToolException(getName(), number, args);
  }

  @Override
  public void addTarget(final IToolTarget target) {
    println("Currently ignoring, since already in config: " + target.getLocation());
  }

  @Override
  public void addToClassPath(final URI loc) {
    config.addToClassPath(loc);
  }

  @Override
  public ArtifactGenerator getGenerator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reportError(final String msg, final Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void reportError(final String msg) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setOption(final String key, final String option) {
    println("Currently ignoring, since already in config: " + key);
  }

  @SuppressWarnings("unused")
  private void setupCustomClassLoader(final boolean debug, final CommandlineJava cmdj) {
    // String tools = getPluginDir(debug,
    // SierraToolConstants.TOOL_PLUGIN_ID);
    File commonLoading = new File(config.getToolsDirectory(), "common-loading.jar");
    cmdj.createVmArgument().setValue("-Xbootclasspath/a:" + commonLoading.getAbsolutePath());
    cmdj.createVmArgument().setValue("-Djava.system.class.loader=com.surelogic.common.loading.CustomClassLoader");

    try {
      if (TestCode.BAD_AUX_PATH.equals(testCode)) {
        throw new IOException("Testing error with aux path");
      }
      File auxPathFile = File.createTempFile("auxPath", ".txt");
      PrintWriter pw = new PrintWriter(auxPathFile);
      cmdj.createVmArgument().setValue("-D" + SierraToolConstants.AUX_PATH_PROPERTY + "=" + auxPathFile.getAbsolutePath());

      // FIX to support PMD's type resolution
      for (IToolTarget t : config.getTargets()) {
        if (t.getType() == IToolTarget.Type.AUX) {
          // path.add(new Path(proj, new
          // File(t.getLocation()).getAbsolutePath()));
          pw.println(t.getLocation().toURL());
        }
      }
      pw.close();
      auxPathFile.deleteOnExit();
    } catch (IOException e) {
      throw new ToolException(getName(), SierraToolConstants.ERROR_CREATING_AUX_PATH, e);
    }
  }

  private File setupConfigFile(final CommandlineJava cmdj) {
    try {
      if (TestCode.BAD_CONFIG.equals(testCode)) {
        throw new JAXBException("Testing error with config file");
      }
      File file = File.createTempFile("config", ".xml");
      file.deleteOnExit();

      if (marshaller == null) {
        System.out.println("Couldn't create config file");
        return null;
      }
      OutputStream out = new FileOutputStream(file);
      marshaller.marshal(config, out);
      out.close();

      cmdj.createVmArgument().setValue("-D" + SierraToolConstants.CONFIG_PROPERTY + "=" + file.getAbsolutePath());
      /*
       * // Check to see if the config is the same try { XMLInputFactory xmlif =
       * XMLInputFactory.newInstance(); XMLStreamReader xmlr =
       * xmlif.createXMLStreamReader(new FileReader(file)); Unmarshaller
       * unmarshaller = ctx.createUnmarshaller(); final Config c =
       * unmarshaller.unmarshal(xmlr, Config.class).getValue(); if
       * (c.getPluginDirs().size() != config.getPluginDirs().size()) {
       * System.out.println("Couldn't match size of plugin dirs"); } else {
       * System.out.println("Same"); } } catch(Exception e) {
       * e.printStackTrace(); }
       */
      return file;
    } catch (IOException e) {
      throw new ToolException(getName(), SierraToolConstants.ERROR_CREATING_CONFIG, e);
    } catch (JAXBException e) {
      throw new ToolException(getName(), SierraToolConstants.ERROR_CREATING_CONFIG, e);
    }
  }

  @Override
  protected Class<? extends AbstractRemoteSLJob> getRemoteClass() {
    return RemoteTool.class;
  }

  @Override
  protected void finishSetupJVM(final boolean debug, final CommandlineJava cmdj, final Project proj) {
    setupConfigFile(cmdj);
    // setupCustomClassLoader(debug, cmdj);
    // cmdj.createBootclasspath(proj);

    // cmdj.createVmArgument().setValue("-Dfindbugs.debug.PluginLoader=true");
    // cmdj.createVmArgument().setValue("-Dfindbugs.verbose=true");
    // cmdj.createVmArgument().setValue("-Dfindbugs.debug=true");
    // cmdj.createVmArgument().setValue("-Dfindbugs.execplan.debug=true");
    cmdj.createVmArgument().setValue("-D" + ToolUtil.TOOLS_PATH_PROP_NAME + "=" + ToolUtil.getSierraToolDirectory());
  }

  @Override
  protected void setupClassPath(final ConfigHelper util, final CommandlineJava cmdj, final Project proj, final Path path) {
    util.addPluginToPath(COMMON_PLUGIN_ID);
    util.addPluginJarsToPath(COMMON_PLUGIN_ID, "lib/runtime/commons-lang3-3.3.2.jar");

    // sierra-tool needs special handling since it is unpacked, due to
    // Reckoner (and other tools)
    util.addPluginToPath(SierraToolConstants.TOOL_PLUGIN_ID);
    util.addPluginToPath(SierraToolConstants.MESSAGE_PLUGIN_ID);

    /*
     * Previously used by PMD 5 -- no longer in v.5.2.3
     * 
     * if (util.addPluginJarsToPath(SierraToolConstants.JUNIT4_PLUGIN_ID, true,
     * "junit.jar", "junit-4.1.jar")) { // Called just to mark it as "used";
     * util.getPluginDir(SierraToolConstants.JUNIT_PLUGIN_ID, false); } else {
     * util.addPluginJarsToPath(SierraToolConstants.JUNIT_PLUGIN_ID,
     * "junit.jar"); }
     */
    addToolPluginJars(util.debug, util.getPath());

    for (File jar : util.getPath()) {
      addToPath(proj, path, jar, true);
    }
    /*
     * if (false) { for(String elt : path.list()) { System.out.println("Path: "
     * +elt); } }
     */
  }

  private void addToolPluginJars(final boolean debug, final Collection<File> path) {
    // TODO need to deactivate some?
    for (IToolFactory f : ToolUtil.findToolFactories()) {
      for (File jar : f.getRequiredJars(config)) {
        path.add(jar);
      }
    }
  }

  public IToolInstance create() {
    throw new UnsupportedOperationException();
  }

  public IToolInstance create(final String name, final ILazyArtifactGenerator generator) {
    throw new UnsupportedOperationException();
  }

  public Set<ArtifactType> getArtifactTypes() {
    return Collections.emptySet();
  }

  public List<File> getRequiredJars() {
    return Collections.emptyList();
  }

  @Override
  public String getHTMLInfo() {
    return factory.getHTMLInfo();
  }

  @Override
  public String getId() {
    return factory.getId();
  }

  @Override
  public String getVersion() {
    return factory.getVersion();
  }

  public static void main(final String[] args) {
    try {
      JAXBContext ctx = JAXBContext.newInstance(Config.class);
      Config c = new Config();
      File file = File.createTempFile("sdfsda", "xml");
      file.deleteOnExit();

      OutputStream out = new FileOutputStream(file);
      ctx.createMarshaller().marshal(c, out);
      out.close();

      Config c2 = (Config) ctx.createUnmarshaller().unmarshal(file);
      System.out.println(c.equals(c2));
    } catch (JAXBException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
