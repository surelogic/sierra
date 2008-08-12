package com.surelogic.sierra.tool;

import java.io.File;
import java.net.*;
import java.util.*;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractToolInstance extends AbstractSLJob implements IToolInstance {
  private final ITool tool;
  protected final ArtifactGenerator generator;  
  private final List<IToolTarget> srcTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> binTargets = new ArrayList<IToolTarget>();
  private final List<IToolTarget> auxTargets = new ArrayList<IToolTarget>();
  private final List<URI> paths = new ArrayList<URI>();
  private boolean done = false;
  private final boolean closeWhenDone;
  private final Map<String,String> options = new HashMap<String,String>();
  protected final boolean debug;
  protected final SLStatus.Builder status = new SLStatus.Builder();
  
  protected AbstractToolInstance(boolean debug, ITool t, ArtifactGenerator gen, boolean close) {
	super(t.getName()); // FIX is this the right name?
    tool = t;
    generator = gen;
    closeWhenDone = close;
    this.debug = debug;
  }
  
  public final ArtifactGenerator getGenerator() {
    return generator;
  }
  
  private void checkArgs(Object arg) {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    if (arg == null) {
      throw new IllegalArgumentException("Null argument");
    }
  }
  
  public final void addTarget(IToolTarget target) {
    checkArgs(target);
    switch (target.getType()) {
      case SOURCE:
        srcTargets.add(target);
        break;
      case BINARY:
        binTargets.add(target);
        break;
      case AUX:
        auxTargets.add(target);
        break;
      default:
    	System.out.println("Ignoring target "+target.getLocation());
    }
  }
  
  public final void addToClassPath(URI loc) {
    checkArgs(loc);
    paths.add(loc);
  }

  public final Iterable<IToolTarget> getSrcTargets() {
    return srcTargets;
  }
  
  public final Iterable<IToolTarget> getBinTargets() {
    return binTargets;
  }
  
  public final Iterable<IToolTarget> getAuxTargets() {
    return auxTargets;
  }
  
  protected final Iterable<URI> getPaths() {
    return paths;
  }
  
  public final SLStatus run(SLProgressMonitor monitor) {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    // monitor.setTaskName("Scanning with "+getName()+" v"+getVersion());
    if (debug) {
    	for(IToolTarget t : getSrcTargets()) {
    		System.out.println("Source: "+t.getLocation());
    	}
    	for(IToolTarget t : getBinTargets()) {
    		System.out.println("Binary: "+t.getLocation());
    	}
    	for(IToolTarget t : getAuxTargets()) {
    		System.out.println("Auxiliary: "+t.getLocation());
    	}
    }
    try {
      status.addChild(execute(monitor));
    }
    catch(Exception e) {
      reportError("Exception during run()", e);
    }
    finally {
      done = true;
    }
    monitor.done();

    if (closeWhenDone) {
      generator.finished(monitor);
      monitor.done();
    }
    return status.build();
  }

  public final void reportError(String msg) {
    generator.error().message(msg).tool(getName());
    status.addChild(SLStatus.createErrorStatus(-1, msg));  
  }
  
  public final void reportError(String msg, Throwable e) {
    StringBuilder sb = new StringBuilder();
    sb.append(msg).append('\n');
    sb.append(e.getClass().getName()).append(' ').append(e.getMessage()).append('\n');
    for(StackTraceElement ste : e.getStackTrace()) {
      sb.append("\tat ").append(ste).append('\n');
    }
    generator.error().message(sb.toString()).tool(getName());
    status.addChild(SLStatus.createErrorStatus(-1, msg, e));
  }
  
  protected abstract SLStatus execute(SLProgressMonitor mon) throws Exception;    
  
  /**************** ITool **********************/
  public final String getHtmlDescription() {
    return tool.getHtmlDescription();
  }

  public final String getName() {
    return tool.getName();
  }

  public final String getTitle() {
    return tool.getTitle();
  }

  public final String getVersion() {
    return tool.getVersion();
  } 
  
  public final Set<String> getArtifactTypes() {
    return tool.getArtifactTypes();
  }
  
  public final IToolInstance create(final Config config) {
    throw new UnsupportedOperationException("Instances can't create other instances");
  }
  
  public final IToolInstance create(final ArtifactGenerator generator) {
    throw new UnsupportedOperationException("Instances can't create other instances");
  }
  
  public void setOption(String key, String value) {
	if (key == null) {
	  throw new IllegalArgumentException("null key");
	}
	options.put(key, value);
  }
  protected String getOption(String key) {
	return options.get(key);
  }
  
  public interface SourcePrep {
	void prep(File f);	  
  }  
  protected void prepJavaFiles(SourcePrep p) {
	  for (IToolTarget t : getSrcTargets()) {
		  for (URI loc : t.getFiles()) {
			  File f = new File(loc);
			  if (f.exists() && f.getName().endsWith(".java")) {
				  p.prep(f);
			  }
		  }
	  }  
  }
}
