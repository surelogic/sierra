/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.xml.bind.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

public class LocalTool extends AbstractTool {
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
      Project proj = new Project();
      
      CommandlineJava cmdj   = new CommandlineJava();
      cmdj.setMaxmemory("1024m");
      cmdj.setClassname(RemoteTool.class.getCanonicalName());     
      Path path = cmdj.createClasspath(proj);
      path.add(new Path(proj, "C:/work/workspace/common/common.jar"));
      path.add(new Path(proj, "C:/work/workspace/sierra-tool/sierra-tool.jar"));
      path.add(new Path(proj, "C:/work/workspace/sierra-message/sierra-message.jar"));
      path.add(new Path(proj, "C:/work/workspace/sierra-message/jax-ws/sjsxp.jar"));
      findJars(proj, path, "C:/work/workspace/sierra-message/jaxb");
      findJars(proj, path, "C:/work/workspace/sierra-tool/Tools/pmd/lib");
      findJars(proj, path, "C:/work/workspace/sierra-tool/Tools/FB/lib");
      findJars(proj, path, "C:/work/workspace/sierra-tool/Tools/reckoner/lib");
      path.add(new Path(proj, "C:/work/workspace/sierra-tool/Tools/reckoner/reckoner.jar"));
      
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
        System.out.println(br.readLine());

        JAXBContext ctx = JAXBContext.newInstance(Config.class, 
                                                  JarTarget.class, 
                                                  FullDirectoryTarget.class, 
                                                  FilteredDirectoryTarget.class);
        Marshaller marshaller = ctx.createMarshaller();       
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
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
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void findJars(Project proj, Path path, String folder) {
      for(File f : new File(folder).listFiles()) {
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
