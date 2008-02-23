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
    
    LocalInstance(Config c, SLProgressMonitor mon) {
      config = c;
      monitor = mon;
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
    
    private Set<String> usedPlugins = new HashSet<String>();
    
    private String getPluginDir(final boolean debug, final String pluginId) {
      final String pluginDir = config.getPluginDir(pluginId);
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
          throw new IllegalArgumentException("Does not exist: "+name);
        }
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

    private void addPluginJarsToPath(final boolean debug, Project proj, Path path,
                                     final String pluginId, String... jars) {
      addPluginJarsToPath(debug, proj, path, pluginId, false, jars);
    }
    
    /**
     * @param exclusive If true, try each of the jars in sequence until one exists
     */
    private void addPluginJarsToPath(final boolean debug, Project proj, Path path,
                                     final String pluginId, boolean exclusive, String... jars) {
      final String pluginDir = getPluginDir(debug, pluginId);
      for(String jar : jars) {
        boolean exists = addToPath(proj, path, pluginDir+'/'+jar, !exclusive);
        if (exclusive && exists) {
          return;
        }
      }
    }
    
    private void addAllPluginJarsToPath(final boolean debug, Project proj, Path path,
        final String pluginId, String libPath) {
      final String pluginDir = getPluginDir(debug, pluginId);
      findJars(proj, path, pluginDir+'/'+libPath);
    }
    
    public void run() {
      final boolean debug = LOG.isLoggable(Level.FINE);
      Project proj = new Project();
      
      CommandlineJava cmdj   = new CommandlineJava();
      cmdj.setMaxmemory("1024m");
      cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");    
      if (false) {
        cmdj.createVmArgument().setValue("-verbose");
      }
      cmdj.setClassname(RemoteTool.class.getCanonicalName());     
      Path path = cmdj.createClasspath(proj);
      addPluginToPath(debug, proj, path, SierraToolConstants.COMMON_PLUGIN_ID);
      // sierra-tool needs special handling since it is unpacked, due to Reckoner (and other tools)
      addPluginToPath(debug, proj, path, SierraToolConstants.TOOL_PLUGIN_ID, true);
      addPluginToPath(debug, proj, path, SierraToolConstants.MESSAGE_PLUGIN_ID);
      
      // JAXB is included in Java 6 and beyond
      if (SystemUtils.IS_JAVA_1_5) {     
        addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.JAVA5_PLUGIN_ID, "lib/jaxb");
      }
      addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.PMD_PLUGIN_ID, "lib");
      addAllPluginJarsToPath(debug, proj, path, SierraToolConstants.FB_PLUGIN_ID, "lib");      
      findJars(proj, path, new File(config.getToolsDirectory(), "reckoner/lib"));
      path.add(new Path(proj, new File(config.getToolsDirectory(), 
                                       "reckoner/reckoner.jar").getAbsolutePath()));
      addPluginJarsToPath(debug, proj, path, SierraToolConstants.JUNIT4_PLUGIN_ID, true,
                          "junit.jar", "junit-4.1.jar");
      addPluginJarsToPath(debug, proj, path, SierraToolConstants.JUNIT_PLUGIN_ID,
                          "junit.jar");
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
        }
      }
      // FIX to support PMD's type resolution
      for(IToolTarget t : config.getTargets()) {
        if (t.getType() == IToolTarget.Type.AUX) {
          path.add(new Path(proj, new File(t.getLocation()).getAbsolutePath()));
        }
      }
      if (debug) {
        System.out.println("Starting process:");
        for(String arg : cmdj.getCommandline()) {
          System.out.println("\t"+arg);
        }
      }
      ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());      
      pb.redirectErrorStream(true);      
      /*
      Map<String, String> env = pb.environment();
      env.put("VAR1", "myValue");
      env.remove("OTHERVAR");
      env.put("VAR2", env.get("VAR1") + "suffix");
      pb.directory(new File("myDir"));
      */
      
      try {
        Process p         = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String firstLine  = br.readLine();
        if (debug) {
          while (firstLine != null) {
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
          throw new NullPointerException("No input from the remote JVM (possibly a classpath issue)");
        }

        File file = File.createTempFile("config", ".xml");
        file.deleteOnExit();

        OutputStream out = new FileOutputStream(file);
        marshaller.marshal(config, out);
        out.close();
        
        // Send the location of the config file
        final PrintStream pout = new PrintStream(p.getOutputStream());
        pout.println(file.getAbsolutePath());
        pout.println();
        pout.flush();
        
        // Copy any output 
        String line = br.readLine();
      loop:
        while (line != null) {
          if (monitor.isCanceled()) {
            pout.println("##"+Local.CANCEL);
            p.destroy();
            throw new InterruptedException("Scan was cancelled");
          }
          
          System.out.println(line);
          if (line.startsWith("##")) {
            StringTokenizer st = new StringTokenizer(line, "#,");
            if (st.hasMoreTokens()) {            
              String first = st.nextToken();
              switch (Remote.valueOf(first)) {
                case TASK:
                  monitor.beginTask(st.nextToken(), Integer.valueOf(st.nextToken().trim()));
                  break;
                case SUBTASK:
                  monitor.subTask(st.nextToken());
                  break;
                case WORK:
                  monitor.worked(Integer.valueOf(st.nextToken().trim()));
                  break;
                case ERROR:
                  line = copyException(first, st.nextToken(), br);
                  break;
                case FAILED:
                  line = copyException(first, st.nextToken(), br);
                  System.out.println("Terminating run");
                  break loop;
                case DONE:
                  monitor.done();
                  break loop;
                default:                
              }
            }
          }
          line = br.readLine();
        }
        line = br.readLine();
        if (line != null) {
          System.out.println(line);
        }
        System.out.println("Process result = "+p.waitFor());
        br.close();
        pout.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private String copyException(String type, String msg, BufferedReader br) throws IOException {
      StringBuilder sb = new StringBuilder(type);      
      System.out.println(msg);
      sb.append(": ").append(msg).append('\n');
      
      String line = br.readLine();
      while (line != null && line.startsWith("\t")) {
        System.out.println(line);
        sb.append(msg).append('\n');
        line = br.readLine();
      }
      monitor.error(sb.toString());
      if (line != null) {
        System.out.println(line);
      }
      return line;
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
