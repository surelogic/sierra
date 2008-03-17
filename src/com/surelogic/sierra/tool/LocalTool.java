/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.*;

import org.apache.commons.lang.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

public class LocalTool extends AbstractTool {
  private static final JAXBContext ctx = createContext();
  
  private static final Marshaller marshaller = createMarshaller(ctx);
  
  private static JAXBContext createContext() {
    try {
      return JAXBContext.newInstance(Config.class, 
        FileTarget.class,
        JarTarget.class, 
        FullDirectoryTarget.class, 
        FilteredDirectoryTarget.class);
    } catch (JAXBException e) {
      LOG.log(Level.SEVERE, "Couldn't create JAXB context", e);
      return null;
    }
  }
  
  private static Marshaller createMarshaller(JAXBContext ctx) {
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
  
  public LocalTool() {
    super("Local", "1.0", "Local", "Local tool for running other tools in another JVM");
  }
  
  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  public IToolInstance create(final Config config, final SLProgressMonitor monitor) {
    return new LocalInstance(config, monitor);
  }
  
  public IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");
  }
  
  protected IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");   
  }
  
  private static class LocalInstance extends LocalTool implements IToolInstance {
    final Config config;    
    final SLProgressMonitor monitor;
    final TestCode testCode;
    
    LocalInstance(Config c, SLProgressMonitor mon) {
      config = c;
      monitor = mon;
      testCode = getTestCode(c.getTestCode());
    }

    public void addTarget(IToolTarget target) {
      config.addTarget((ToolTarget) target);
    }

    public void addToClassPath(URI loc) {
      config.addToClassPath(loc);
    }

    public ArtifactGenerator getGenerator() {
      // TODO Auto-generated method stub
      return null;
    }

    public SLProgressMonitor getProgressMonitor() {
      // TODO Auto-generated method stub
      return null;
    }

    public void reportError(String msg, Throwable t) {
      // TODO Auto-generated method stub
      
    }

    public void reportError(String msg) {
      // TODO Auto-generated method stub
      
    }
    
    public void setOption(String key, String option) {
      System.out.println("Currently ignoring, since already in config: "+key);
    }
    
    private Set<String> usedPlugins = new HashSet<String>();
    
    private String getPluginDir(final boolean debug, final String pluginId) {
      return getPluginDir(debug, pluginId, true); 
    }
    
    private String getPluginDir(final boolean debug, final String pluginId, boolean required) {
      final String pluginDir = config.getPluginDir(pluginId, required);
      if (debug) {
        System.out.println(pluginId+" = "+pluginDir);
      }
      usedPlugins.add(pluginId);
      return pluginDir;
    }
    private void addToPath(Project proj, Path path, String name) {
      addToPath(proj, path, name, true);
    }
    
    private boolean addToPath(Project proj, Path path, String name, boolean required) {
      final File f = new File(name);
      final boolean exists = f.exists();
      if (!exists) {
        if (required) {
          throw new ToolException(SierraToolConstants.ERROR_CODE_MISSING_FOR_TOOL, name);
        }
      } else if (TestCode.MISSING_CODE.equals(testCode)) {
        throw new ToolException(SierraToolConstants.ERROR_CODE_MISSING_FOR_TOOL, name);
      } else {
        path.add(new Path(proj, name));
      }
      return exists;
    }
    
    private void addPluginToPath(final boolean debug, Project proj, Path path,
        final String pluginId) {
      addPluginToPath(debug, proj, path, pluginId, false);
    }
    
    private void addPluginToPath(final boolean debug, Project proj, Path path,
                                 final String pluginId, boolean unpacked) {
      final String pluginDir = getPluginDir(debug, pluginId);
      if (unpacked) {
        boolean workspaceExists = addToPath(proj, path, pluginDir+"/bin", false); // in workspace
        if (!workspaceExists) {
          addToPath(proj, path, pluginDir); // as plugin
        }
      }
      else if (pluginDir.endsWith(".jar")) {
        addToPath(proj, path, pluginDir); // as plugin
      } else {
        addToPath(proj, path, pluginDir+"/bin"); // in workspace
      }
    }

    /**
     * @return true if found
     */
    private boolean addPluginJarsToPath(final boolean debug, Project proj, Path path,
                                     final String pluginId, String... jars) {
      return addPluginJarsToPath(debug, proj, path, pluginId, false, jars);
    }
    
    /**
     * @param exclusive If true, try each of the jars in sequence until one exists
     * @return true if found
     */
    private boolean addPluginJarsToPath(final boolean debug, Project proj, Path path,
                                        final String pluginId, boolean exclusive, String... jars) {
      boolean rv = true;
      final String pluginDir = getPluginDir(debug, pluginId);
      for(String jar : jars) {
        boolean exists = addToPath(proj, path, pluginDir+'/'+jar, !exclusive);
        if (exclusive && exists) {
          return true;
        }
        rv = rv && exists;
      }
      return rv;
    }
    
    private void addAllPluginJarsToPath(final boolean debug, Project proj, Path path,
        final String pluginId, String libPath) {
      final String pluginDir = getPluginDir(debug, pluginId);
      findJars(proj, path, pluginDir+'/'+libPath);
    }
    
    @SuppressWarnings("unused")
	private void setupCustomClassLoader(final boolean debug,
        CommandlineJava cmdj) {
      //String tools = getPluginDir(debug, SierraToolConstants.TOOL_PLUGIN_ID);
      File commonLoading = new File(config.getToolsDirectory(), "common-loading.jar");
      cmdj.createVmArgument().setValue("-Xbootclasspath/a:"+commonLoading.getAbsolutePath());
      cmdj.createVmArgument().setValue("-Djava.system.class.loader=com.surelogic.common.loading.CustomClassLoader");
      try {
        if (TestCode.BAD_AUX_PATH.equals(testCode)) {
          throw new IOException("Testing error with aux path");
        }
        File auxPathFile = File.createTempFile("auxPath", ".txt");
        PrintWriter pw = new PrintWriter(auxPathFile);
        cmdj.createVmArgument().setValue("-D"+SierraToolConstants.AUX_PATH_PROPERTY+"="+
        		                         auxPathFile.getAbsolutePath());
      
        // FIX to support PMD's type resolution
        for(IToolTarget t : config.getTargets()) {
          if (t.getType() == IToolTarget.Type.AUX) {
            // path.add(new Path(proj, new File(t.getLocation()).getAbsolutePath()));
            pw.println(t.getLocation().toURL());
          }
        }
        pw.close();
        auxPathFile.deleteOnExit();
      } catch(IOException e) {
        throw new ToolException(SierraToolConstants.ERROR_CREATING_AUX_PATH, e);
      }
    }
 
    private File setupConfigFile(CommandlineJava cmdj) {
      if (config.getTestCode() != null) {
        cmdj.createVmArgument().setValue("-D"+SierraToolConstants.TEST_CODE_PROPERTY+"="+config.getTestCode());
      }
      try {
        if (TestCode.BAD_CONFIG.equals(testCode)) {
          throw new JAXBException("Testing error with config file");
        }
        File file = File.createTempFile("config", ".xml");
        file.deleteOnExit();

        OutputStream out = new FileOutputStream(file);
        marshaller.marshal(config, out);
        out.close();
        
        cmdj.createVmArgument().setValue("-D"+SierraToolConstants.CONFIG_PROPERTY+"="+file.getAbsolutePath());
        return file;
      } catch(IOException e) {
        throw new ToolException(SierraToolConstants.ERROR_CREATING_CONFIG, e);
      } catch(JAXBException e) {
        throw new ToolException(SierraToolConstants.ERROR_CREATING_CONFIG, e);
      }
    }
    
    private void setupJVM(final boolean debug, final CommandlineJava cmdj) {
      final Project proj = new Project();      
      setupConfigFile(cmdj);      
      if (TestCode.LOW_MEMORY.equals(testCode)) {
        cmdj.setMaxmemory("2m");
      }
      else if (TestCode.HIGH_MEMORY.equals(testCode)) {
        cmdj.setMaxmemory("2048m");
      }
      else if (TestCode.MAD_MEMORY.equals(testCode)) {
        cmdj.setMaxmemory("9999m");
      }
      else if (config.getMemorySize() > 0) {
        cmdj.setMaxmemory(config.getMemorySize()+"m");
      } 
      else {
        cmdj.setMaxmemory("1024m");
      }      
      cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");    
      // setupCustomClassLoader(debug, cmdj);
      if (false) {
        cmdj.createVmArgument().setValue("-verbose");
      }
      cmdj.setClassname(RemoteTool.class.getCanonicalName());     
      //cmdj.createBootclasspath(proj);
      Path path = cmdj.createClasspath(proj);
      addPluginToPath(debug, proj, path, SierraToolConstants.COMMON_PLUGIN_ID);
      // sierra-tool needs special handling since it is unpacked, due to Reckoner (and other tools)
      addPluginToPath(debug, proj, path, SierraToolConstants.TOOL_PLUGIN_ID, true);
      addPluginToPath(debug, proj, path, SierraToolConstants.MESSAGE_PLUGIN_ID);
      
      // JAXB is included in Java 6 and beyond
      if (SystemUtils.IS_JAVA_1_5) {     
        addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.JAVA5_PLUGIN_ID, "lib/jaxb");
      } else {
        // Called just to mark it as "used";
        getPluginDir(debug, SierraToolConstants.JAVA5_PLUGIN_ID, false);
      }
      addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.PMD_PLUGIN_ID, "lib");
      addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.FB_PLUGIN_ID, "lib");      
      findJars(proj, path, new File(config.getToolsDirectory(), "reckoner/lib"));
      path.add(new Path(proj, new File(config.getToolsDirectory(), 
                                       "reckoner/reckoner.jar").getAbsolutePath()));
      if (addPluginJarsToPath(debug, proj, path, SierraToolConstants.JUNIT4_PLUGIN_ID, true,
                               "junit.jar", "junit-4.1.jar")) {
        // Called just to mark it as "used";
        getPluginDir(debug, SierraToolConstants.JUNIT_PLUGIN_ID, false);
      } else {
        addPluginJarsToPath(debug, proj, path, SierraToolConstants.JUNIT_PLUGIN_ID,
                            "junit.jar");
      }
      // Add all the plugins needed by Reckoner (e.g. JDT Core and company)
      for(String id : config.getPluginDirs().keySet()) {
        if (usedPlugins.contains(id)) {
          continue;
        }
        addPluginToPath(debug, proj, path, id);
      }
      
      // TODO convert into error if things are really missing
      if (debug) {
        for(String p : path.list()) {
          if (!new File(p).exists()) {
            System.out.println("Does not exist: "+p);
          }
          else if (debug) {
            System.out.println("Path: "+p);
          }
        }
      }
    }
    
    private static final int FIRST_LINES = 3;
    
    private String currentTask = "(unknown)";
    
    public void run() {
      final boolean debug = LOG.isLoggable(Level.FINE);
      CommandlineJava cmdj   = new CommandlineJava();
      setupJVM(debug, cmdj);

      if (debug) {
        System.out.println("Starting process:");
        for(String arg : cmdj.getCommandline()) {
          System.out.println("\t"+arg);
        }
      }      
      ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());      
      pb.redirectErrorStream(true);            
      try {
        Process p         = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String firstLine  = br.readLine();
        if (debug) {
          while (firstLine != null) {
            // Copy verbose output until we get to the first line from RemoteTool
            if (firstLine.startsWith("[")) {
              //if (!firstLine.endsWith("rt.jar]")) {
              System.out.println(firstLine);
              //}
              firstLine = br.readLine();
            } else {
              break;
            }
          }
        }
        System.out.println("First line = "+firstLine);        
        
        if (firstLine == null) {
          throw new ToolException(SierraToolConstants.ERROR_NO_OUTPUT_FROM_TOOLS);
        }
        final String[] firstLines = new String[FIRST_LINES];
        int numLines = 1;
        firstLines[0] = firstLine;
        
        // Copy any output 
        final PrintStream pout = new PrintStream(p.getOutputStream());
        if (TestCode.SCAN_CANCELLED.equals(testCode)) {
          cancel(p, pout);
        }
        
        String line = br.readLine();
      loop:
        while (line != null) {
          if (numLines < FIRST_LINES) {
            firstLines[numLines] = line;
            numLines++;
          }          
          if (monitor.isCanceled()) {
            cancel(p, pout);
          }
          
          if (line.startsWith("##")) {
            StringTokenizer st = new StringTokenizer(line, "#,");
            if (st.hasMoreTokens()) {            
              String first = st.nextToken();
              switch (Remote.valueOf(first)) {
                case TASK:
                  System.out.println(line);
                  final String task = currentTask = st.nextToken();
                  final String work = st.nextToken();
                  // LOG.info(task+": "+work);
                  monitor.beginTask(task, Integer.valueOf(work.trim()));
                  break;
                case SUBTASK:
                  monitor.subTask(st.nextToken());
                  break;
                case WORK:
                  monitor.worked(Integer.valueOf(st.nextToken().trim()));
                  break;
                case ERROR:
                  System.out.println(line);
                  copyException(first, st.nextToken(), br);
                  break;
                case FAILED:
                  System.out.println(line);
                  String msg = copyException(first, st.nextToken(), br);
                  System.out.println("Terminating run");
                  p.destroy();
                  if (msg.contains("FAILED:  java.lang.OutOfMemoryError")) {
                    throw new ToolException(SierraToolConstants.ERROR_MEMORY_SIZE_TOO_SMALL, 
                                            config.getMemorySize());
                  }
                  throw new RuntimeException(msg);
                case DONE:
                  System.out.println(line);
                  monitor.done();
                  break loop;
                default:                
                  System.out.println(line);
              }
            } else {
              System.out.println(line);
            }
          } else {
            System.out.println(line);
          }
          line = br.readLine();
        }
        line = br.readLine();
        if (line != null) {
          System.out.println(line);
        } 
        // See if the process already died?
        int value = handleExitValue(p);
        br.close();
        pout.close();
        if (value != 0) {
          examineFirstLines(firstLines);
          throw new ToolException(SierraToolConstants.ERROR_PROCESS_FAILED, value);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void cancel(Process p, final PrintStream pout) {
      pout.println("##"+Local.CANCEL);
      p.destroy();
      throw new ToolException(SierraToolConstants.ERROR_SCAN_CANCELLED);
    }

    private void examineFirstLines(String[] firstLines) {
      for(String line : firstLines) {
        if (line.startsWith("Could not reserve enough space") ||
            line.startsWith("Invalid maximum heap size")) {
          throw new ToolException(SierraToolConstants.ERROR_MEMORY_SIZE_TOO_BIG, config.getMemorySize());
        }
      }      
    }

    private int handleExitValue(Process p) {
      int value;
      try {
        value = p.exitValue();
        System.out.println("Process result after waiting = "+value);
      } catch (IllegalThreadStateException e) {
        // Not done yet
        final Thread currentThread = Thread.currentThread(); 
        Thread t = new Thread() {            
          public void run() {
            // Set to timeout in 1 minute
            try {
              Thread.sleep(60000);
              currentThread.interrupt();
            } catch (InterruptedException e) {
              // Just end
            }
          }
        };
        
        final long start = System.currentTimeMillis();
        t.start();
        try {
          value = p.waitFor();
          t.interrupt();
        } catch(InterruptedException ie) {
          long time = System.currentTimeMillis() - start;
          throw new RuntimeException("Timeout waiting for process to exit: "+time+" ms");
        }
        System.out.println("Process result after waiting = "+value);
      }
      return value;
    }
    
    private String copyException(String type, String msg, BufferedReader br) throws IOException {
      StringBuilder sb = new StringBuilder(currentTask+' '+type);      
      System.out.println(msg);
      sb.append(": ").append(msg).append('\n');
      
      String line = br.readLine();
      while (line != null && line.startsWith("\t")) {
        System.out.println(line);
        sb.append(' ').append(line).append('\n');
        line = br.readLine();
      }
      if (line != null) {
        System.out.println(line);
      }
      final String errMsg = sb.toString();
      monitor.error(errMsg);
      return errMsg;
    }
    
    private void findJars(Project proj, Path path, String folder) {
      findJars(proj, path, new File(folder));
    }
    
    private void findJars(Project proj, Path path, File folder) {
      for(File f : folder.listFiles()) {
        String name = f.getAbsolutePath();
        if (name.endsWith(".jar")) {
          path.add(new Path(proj, name));
        }
      }
    }
  }
  
  public static void main(String[] args) {
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
