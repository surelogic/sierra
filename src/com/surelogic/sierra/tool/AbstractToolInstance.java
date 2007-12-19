package com.surelogic.sierra.tool;

import java.net.URL;
import java.util.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractToolInstance implements IToolInstance {
  private final SLProgressMonitor monitor;
  private List<IToolTarget> targets = new ArrayList<IToolTarget>();
  private List<URL> paths = new ArrayList<URL>();
  private boolean done = false;
  
  protected AbstractToolInstance(SLProgressMonitor m) {
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
    targets.add(target);
  }
  
  public final void addToClassPath(URL loc) {
    checkArgs(loc);
    paths.add(loc);
  }

  protected final Iterator<IToolTarget> getTargets() {
    return targets.listIterator();
  }
  
  protected final Iterator<URL> getPaths() {
    return paths.listIterator();
  }
  
  public void run() {
    if (done) {
      throw new IllegalArgumentException("Tool instance cannot be reused");
    }
    monitor.setTaskName("Starting scan using "+getName()+" v"+getVersion());
    
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
}
