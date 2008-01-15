package com.surelogic.sierra.tool;

import java.net.URI;
import java.util.*;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class MultiTool extends AbstractTool {
  protected List<ITool> tools = new ArrayList<ITool>();
  
  public MultiTool() {
    super("MultiTool", "1.0", "MultiTool", "A container for other tools");
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  protected IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    return new Instance(this, generator, monitor, close);
  }
  
  public void addTool(ITool t) {
    if (t != null && !tools.contains(t)) {
      tools.add(t);
    }
  }
  
  private static class Instance extends MultiTool implements IToolInstance {
    private List<IToolInstance> instances = new ArrayList<IToolInstance>();
    private IToolInstance first = null;

    private ArtifactGenerator generator;
    private SLProgressMonitor monitor;
    private boolean closeWhenDone;
    
    Instance(MultiTool mt, ArtifactGenerator gen, SLProgressMonitor mon, boolean close) {
      for(ITool tool : mt.tools) {
        this.tools.add(tool);
        
        IToolInstance i = tool.create(gen, mon);
        instances.add(i);
        if (first == null) {
          first = i;
        }
      }
      generator = gen;
      monitor = mon;
      closeWhenDone = close;
      
      mon.beginTask("Multiple tools", 100);
      mon.subTask("Setting up scans");
    }

    public void run() {                 
      for(IToolInstance i : instances) {
        i.run();
      }
      if (closeWhenDone) {
        generator.finished(monitor);
        monitor.done();
      }
    }

    public void addTarget(IToolTarget target) {
      for(IToolInstance i : instances) {
        i.addTarget(target);
      }
    }

    public void addToClassPath(URI loc) {
      for(IToolInstance i : instances) {
        i.addToClassPath(loc);
      }
    }

    public ArtifactGenerator getGenerator() {
      return first != null ? first.getGenerator() : null;
    }

    public SLProgressMonitor getProgressMonitor() {
      return first != null ? first.getProgressMonitor() : null;
    }

    public void reportError(String msg, Throwable t) {
      if (first != null) {
        first.reportError(msg, t);
      }
    }

    public void reportError(String msg) {
      if (first != null) {
        first.reportError(msg);
      }
    }    
  }
}
