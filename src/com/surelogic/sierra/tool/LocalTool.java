/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.*;

import javax.xml.bind.*;

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

    public void run() {
      final boolean debug = false; //LOG.isLoggable(Level.FINE);
      Project proj = new Project();
      
      CommandlineJava cmdj   = new CommandlineJava();
      cmdj.setMaxmemory("1024m");
      cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");    
      if (debug) {
        cmdj.createVmArgument().setValue("-verbose");
      }
      cmdj.setClassname(RemoteTool.class.getCanonicalName());     
      Path path = cmdj.createClasspath(proj);
      final String common = config.getPluginDir(SierraToolConstants.COMMON_PLUGIN_ID);
      LOG.info("common = "+common);
      if (common.endsWith(".jar")) {
        path.add(new Path(proj, common)); // as plugin
      } else {
        path.add(new Path(proj, common+"/bin")); // in workspace
      }
      path.add(new Path(proj, config.getToolsDirectory().getParent())); // as plugin
      path.add(new Path(proj, config.getToolsDirectory().getParent()+"/bin")); // in workspace

      final String message = config.getPluginDir(SierraToolConstants.MESSAGE_PLUGIN_ID);
      LOG.info("message = "+message);
      path.add(new Path(proj, message)); // as plugin
      path.add(new Path(proj, message+"/bin")); // in workspace
      findJars(proj, path, message+"/jaxb");

      final String pmd = config.getPluginDir(SierraToolConstants.PMD_PLUGIN_ID);
      LOG.info("pmd = "+pmd);
      findJars(proj, path, new File(pmd+"/lib"));
      final String fb = config.getPluginDir(SierraToolConstants.FB_PLUGIN_ID);
      LOG.info("fb = "+fb);
      findJars(proj, path, new File(fb+"/lib"));
      findJars(proj, path, new File(config.getToolsDirectory(), "reckoner/lib"));
      path.add(new Path(proj, new File(config.getToolsDirectory(), 
                                       "reckoner/reckoner.jar").getAbsolutePath()));

      final String junit4 = config.getPluginDir(SierraToolConstants.JUNIT4_PLUGIN_ID);
      path.add(new Path(proj, junit4+"/junit.jar"));
      path.add(new Path(proj, junit4+"/junit-4.1.jar"));
      
      final String junit = config.getPluginDir(SierraToolConstants.JUNIT_PLUGIN_ID);
      path.add(new Path(proj, junit+"/junit.jar"));
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
      
      LOG.info("Starting process:");
      for(String arg : cmdj.getCommandline()) {
        LOG.info("\t"+arg);
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
                case FAILED:
                  line = copyException(first, st.nextToken(), br);
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
