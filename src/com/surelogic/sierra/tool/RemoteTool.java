/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.targets.*;

public class RemoteTool extends AbstractTool {
  public RemoteTool() {
    super("Remote", "1.0", "Remote", "Remote tool for running other tools in another JVM");
  }
  
  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  public IToolInstance create(final Config config, final SLProgressMonitor monitor) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");
  }
  
  public IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");
  }

  protected IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");   
  }
  
  public static void main(String[] args) {
    try {
      final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("JVM started");

      String configName = br.readLine();
      FileInputStream file = new FileInputStream(configName);
      System.out.println("Got file: "+configName);
      
      JAXBContext ctx = JAXBContext.newInstance(Config.class, 
          JarTarget.class, 
          FullDirectoryTarget.class, 
          FilteredDirectoryTarget.class);
      XMLInputFactory xmlif = XMLInputFactory.newInstance();         
      XMLStreamReader xmlr = xmlif.createXMLStreamReader(file);
      System.out.println("Created reader");      
      Unmarshaller unmarshaller = ctx.createUnmarshaller();
      
      xmlr.nextTag();
      System.out.println("Finding next tag");
      xmlr.require(START_ELEMENT, null, "config");
      System.out.println("Checking for config");

      Config config = unmarshaller.unmarshal(xmlr, Config.class).getValue();
      //Config config = (Config) unmarshaller.unmarshal(file);
      System.out.println("Read config");
      /*
      String line = br.readLine();
      while (line != null) {
        if (line.equals("\n")) {
          break;
        }
        System.out.println(line);
        line = br.readLine();
      }      
      */
      for(URI location : config.getPaths()) {
        System.out.println("URI = "+location);
      }
      for(ToolTarget t : config.getTargets()) {
        System.out.println("Target = "+t.getLocation());
      }
      System.out.println("Excluded tools = "+config.getExcludedToolsList());
      System.out.flush();
      
      final ITool t = ToolUtil.create(config, false);                           
      System.out.println("Java version: "+config.getJavaVersion());
      System.out.println("Rules file: "+config.getPmdRulesFile());
      
      IToolInstance ti = t.create(config, new Monitor(System.out)); 
      System.out.println("Created tool instance");
      setupToolForProject(ti, config);
      System.out.println("Setup tool");
      System.out.flush();
      ti.run();
      System.out.println("Done scanning");
      System.out.flush();
    } catch (Throwable e) {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }

  private static void setupToolForProject(IToolInstance ti, Config config) {
    for(ToolTarget t : config.getTargets()) {
      ti.addTarget(t);
    }
    for(URI path : config.getPaths()) {
      ti.addToClassPath(path);
    }
  }
  
  private static class Monitor implements SLProgressMonitor {
    final PrintStream out;
    
    public Monitor(PrintStream out) {
      this.out = out;
    }

    public void beginTask(String name, int totalWork) {
      out.println("##"+Remote.TASK+", "+name+", "+totalWork);
    }

    public void done() {
      out.println("##"+Remote.DONE);
    }

    public void error(String msg) {
      // TODO Auto-generated method stub
      
    }

    public void error(String msg, Throwable t) {
      // TODO Auto-generated method stub
      
    }

    public void failed(String msg) {
      // TODO Auto-generated method stub
      
    }

    public void failed(String msg, Throwable t) {
      // TODO Auto-generated method stub
      
    }

    public Throwable getFailureTrace() {
      return null;
    }

    public void internalWorked(double work) {}

    public boolean isCanceled() {
      // TODO Auto-generated method stub
      return false;
    }

    public void setCanceled(boolean value) {
      // TODO Auto-generated method stub
      
    }

    public void setTaskName(String name) {
      // TODO Auto-generated method stub
      
    }

    public void subTask(String name) {
      out.println("##"+Remote.SUBTASK+", "+name);
    }

    public void worked(int work) {
      // TODO Auto-generated method stub
      
    }
  }
}
