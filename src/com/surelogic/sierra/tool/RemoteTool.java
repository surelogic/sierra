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
  
  private static final String CANCEL = "##"+Local.CANCEL;
  
  public static void main(String[] args) {
    try {
      final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("JVM started");

      String configName = br.readLine();
      FileInputStream file = new FileInputStream(configName);
      System.out.println("Got file: "+configName);
      
      JAXBContext ctx = JAXBContext.newInstance(Config.class, 
          FileTarget.class,
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
      
      final Monitor mon = new Monitor(System.out);
      checkInput(br, mon, "Created monitor");
      
      IToolInstance ti = t.create(config, mon); 
      checkInput(br, mon, "Created tool instance");
      
      setupToolForProject(ti, config);
      checkInput(br, mon, "Setup tool");

      ti.run();
      checkInput(br, mon, "Done scanning");
    } catch (Throwable e) {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }

  private static void checkInput(final BufferedReader br, final Monitor mon, String msg)
      throws IOException {
    System.out.println(msg);        
    if (br.ready()) {
      String line = br.readLine();
      System.out.println("Received: "+line);
      if (CANCEL.equals(line)) {
        mon.setCanceled(true); 
      }
    }
    System.out.flush();
  }

  private static class Monitor implements SLProgressMonitor {
    final PrintStream out;
    boolean cancelled = false;
    
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
      return cancelled;
    }

    public void setCanceled(boolean value) {
      cancelled = value;
    }

    public void setTaskName(String name) {
      // TODO Auto-generated method stub
      
    }

    public void subTask(String name) {
      out.println("##"+Remote.SUBTASK+", "+name);
    }

    public void worked(int work) {
      out.println("##"+Remote.WORK+", "+work);
    }
  }
}
