package com.surelogic.sierra.tool;

import java.net.*;
import java.util.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractToolInstance implements IToolInstance {
  private final ITool tool;
  private final SLProgressMonitor monitor;
  private List<IToolTarget> srcTargets = new ArrayList<IToolTarget>();
  private List<IToolTarget> binTargets = new ArrayList<IToolTarget>();
  private List<IToolTarget> auxTargets = new ArrayList<IToolTarget>();
  private List<URI> paths = new ArrayList<URI>();
  private boolean done = false;
  
  protected AbstractToolInstance(ITool t, SLProgressMonitor m) {
    tool = t;
    monitor = m;
  }
  
  public final SLProgressMonitor getProgressMonitor() {
    return monitor;
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
    }
  }
  
  public final void addToClassPath(URI loc) {
    checkArgs(loc);
    paths.add(loc);
  }

  protected final Iterable<IToolTarget> getSrcTargets() {
    return srcTargets;
  }
  
  protected final Iterable<IToolTarget> getBinTargets() {
    return binTargets;
  }
  
  protected final Iterable<IToolTarget> getAuxTargets() {
    return auxTargets;
  }
  
  protected final Iterable<URI> getPaths() {
    return paths;
  }
  
  public final void run() {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    monitor.setTaskName("Starting scan using "+getName()+" v"+getVersion());
    
    for(IToolTarget t : getSrcTargets()) {
      System.out.println("Source: "+t.getLocation());
    }
    for(IToolTarget t : getBinTargets()) {
      System.out.println("Binary: "+t.getLocation());
    }
    for(IToolTarget t : getAuxTargets()) {
      System.out.println("Auxiliary: "+t.getLocation());
    }
    try {
      execute();
    }
    catch(Exception e) {
      monitor.failed(e); 
    }
    finally {
      done = true;
    }
    monitor.done();
  }

  protected abstract void execute() throws Exception;    
  
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
  
  public final IToolInstance create(SLProgressMonitor m) {
    throw new UnsupportedOperationException("Instances can't create other instances");
  }
}
