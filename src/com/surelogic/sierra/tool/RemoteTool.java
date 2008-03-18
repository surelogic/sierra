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
import com.surelogic.common.logging.SLLogger;
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
    final TestCode testCode = getTestCode(System.getProperty(SierraToolConstants.TEST_CODE_PROPERTY));
    if (TestCode.NO_TOOL_OUTPUT.equals(testCode)) {
      System.exit(- SierraToolConstants.ERROR_NO_OUTPUT_FROM_TOOLS);
    }
    System.out.println("JVM started");
    System.out.println("Log level: "+SLLogger.LEVEL.get());
    /*
    System.out.println("java.system.class.loader = "+System.getProperty("java.system.class.loader"));
    System.out.println("System classloader = "+ClassLoader.getSystemClassLoader());
    final String auxPathFile = System.getProperty(SierraToolConstants.AUX_PATH_PROPERTY);
    if (auxPathFile != null) {
    	System.out.println(SierraToolConstants.AUX_PATH_PROPERTY+"="+auxPathFile);
    	File auxFile = new File(auxPathFile);
    	if (auxFile.exists()) {
    		// No longer needed after creating the system ClassLoader
    		auxFile.delete();
    	}
    }
    */
    
    long start = System.currentTimeMillis();
    /*
    try {
      Logger LOG = SLLogger.getLogger("sierra");
    } catch(Throwable t) {
      t.printStackTrace();
    }
    */
    
    try {
      final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Created reader");
      
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      System.out.println("Lowered thread priority");
      
      String configName = System.getProperty(SierraToolConstants.CONFIG_PROPERTY);
      if (configName == null) {
        throw new IllegalArgumentException("No config provided");
      }
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

      final Config config = unmarshaller.unmarshal(xmlr, Config.class).getValue();
      //Config config = (Config) unmarshaller.unmarshal(file);
      System.out.println("Read config");
      file.close();
      new File(configName).delete();

//      String line = br.readLine();
//      while (line != null) {
//        if (line.equals("\n")) {
//          break;
//        }
//        System.out.println(line);
//        line = br.readLine();
//      }      
      
      for(URI location : config.getPaths()) {
        System.out.println("URI = "+location);
      }
      for(ToolTarget t : config.getTargets()) {
        System.out.println(t.getType()+" = "+t.getLocation());
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

      switch (testCode) {
        case SCAN_FAILED:
          outputFailure(System.out, "Testing scan failure", new Throwable());
        case ABNORMAL_EXIT:
          System.exit(- SierraToolConstants.ERROR_PROCESS_FAILED);
        case EXCEPTION:        
          throw new Exception("Testing scan exception");
      }
      ti.run();
      long end = System.currentTimeMillis();
      checkInput(br, mon, "Done scanning: "+(end-start)+" ms");      
    } catch (Throwable e) {
      outputFailure(System.out, null, e);
      System.exit(- SierraToolConstants.ERROR_SCAN_FAILED);
    }
  }

  private static void outputFailure(PrintStream out, String msg, Throwable e) {
    StackTraceElement[] trace = e.getStackTrace();
    out.println("Caught exception");
    for (StackTraceElement ste : trace) {
      out.println("\t at "+ste);
    }
    if (msg == null) {
      out.println("##"+Remote.FAILED+", "+e.getClass().getName()+" : "+e.getMessage());
    } else {
      out.println("##"+Remote.FAILED+", "+msg+" - "+e.getClass().getName()+" : "+e.getMessage());
    }
    for (StackTraceElement ste : trace) {
      out.println("\tat "+ste);
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
      out.println("##"+Remote.ERROR+", "+msg);
    }

    public void error(String msg, Throwable t) {
      out.println("##"+Remote.ERROR+", "+msg);
      t.printStackTrace(out);
    }

    public void failed(String msg) {
      setCanceled(true);
      Throwable t = new Throwable();
      outputFailure(out, msg, t);
    }

    public void failed(String msg, Throwable t) {
      setCanceled(true);
      outputFailure(out, msg, t);
    }

    public Throwable getFailureTrace() {
      return null;
    }

    public void internalWorked(double work) {
      // Do nothing
    }

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
