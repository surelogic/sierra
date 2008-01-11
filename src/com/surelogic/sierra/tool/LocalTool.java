/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import java.io.*;
import java.net.URI;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.targets.IToolTarget;

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
  
  private static class LocalInstance extends LocalTool implements IToolInstance {
    final Config config;
    final SLProgressMonitor monitor;

    LocalInstance(Config c, SLProgressMonitor mon) {
      config = c;
      monitor = mon;
    }

    public void addTarget(IToolTarget target) {
      // TODO Auto-generated method stub
      
    }

    public void addToClassPath(URI loc) {
      // TODO Auto-generated method stub
      
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
      cmdj.setClassname(RemoteTool.class.getCanonicalName());     
      Path path = cmdj.createClasspath(proj);
      path.add(new Path(proj, "C:/work/workspace/common/common.jar"));
      path.add(new Path(proj, "C:/work/workspace/sierra-tool/sierra-tool.jar"));
      path.add(new Path(proj, "C:/work/workspace/sierra-message/sierra-message.jar"));
      for(File f : new File("C:/work/workspace/sierra-message/jaxb").listFiles()) {
        String name = f.getAbsolutePath();
        if (name.endsWith(".jar")) {
          path.add(new Path(proj, name));
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
        System.out.println(br.readLine());

        JAXBContext ctx = JAXBContext.newInstance(Config.class);
        Marshaller marshaller = ctx.createMarshaller();       
        // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(config, p.getOutputStream());
        
        //MessageWarehouse.getInstance().writeConfig(config, p.getOutputStream());
        p.getOutputStream().flush();
        p.getOutputStream().write('\n');
        
        String line = br.readLine();
        while (line != null) {
          System.out.println(line);
          line = br.readLine();
        }
        System.out.println("Process result = "+p.waitFor());
      } catch (Exception e) {
        e.printStackTrace();
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
