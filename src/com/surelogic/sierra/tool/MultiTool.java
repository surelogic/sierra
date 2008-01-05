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
  
  public IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor) {
    return new Instance(this, generator, monitor);
  }
  
  public void addTool(ITool t) {
    if (t != null && !tools.contains(t)) {
      tools.add(t);
    }
  }
  
  private static class Instance extends MultiTool implements IToolInstance {
    private List<IToolInstance> instances = new ArrayList<IToolInstance>();
    private IToolInstance first = null;
    
    Instance(MultiTool mt, ArtifactGenerator gen, SLProgressMonitor mon) {
      for(ITool tool : mt.tools) {
        this.tools.add(tool);
        
        IToolInstance i = tool.create(gen, mon);
        instances.add(i);
        if (first == null) {
          first = i;
        }
      }
    }

    public void run() {
      for(IToolInstance i : instances) {
        i.run();
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
