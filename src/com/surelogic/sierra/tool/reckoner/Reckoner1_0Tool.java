package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.metrics.Reckoner;
import com.surelogic.sierra.metrics.model.Metrics;
import com.surelogic.sierra.tool.AbstractTool;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class Reckoner1_0Tool extends AbstractTool {
  public Reckoner1_0Tool() {
    super("Reckoner", "1.0", "Reckoner", "");
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  private static final boolean batch = false;
  
  private interface Filter {
    boolean exclude(Metrics m);
  }
  
  protected IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {     
        final Reckoner r = new Reckoner();
        final IProgressMonitor mon = new IProgressMonitorWrapper(monitor);
        if (batch) {
          List<File> targets = new ArrayList<File>();
          for(IToolTarget t : getSrcTargets()) {
            targets.add(new File(t.getLocation()));
          }
          buildMetrics(r, mon, targets, new Filter() {
            public boolean exclude(Metrics m) {
              final String path = m.getPath();
              for(IToolTarget t : getSrcTargets()) {
                if (targetExcludes(t, path)) {
                  return true;
                }
              }
              return false;
            }
            
          });
        } else {
          // Do one target at a time
          List<File> targets = new ArrayList<File>(1);
          for(final IToolTarget t : getSrcTargets()) {
            targets.add(new File(t.getLocation()));
            
            buildMetrics(r, mon, targets, new Filter() {
              public boolean exclude(Metrics m) {
                return targetExcludes(t, m.getPath());
              }             
            });
            targets.clear();
          }
        }
      }

      private boolean targetExcludes(IToolTarget t, String path) {
        File root = new File(t.getLocation());
        String rootPath = root.getAbsolutePath();
        if (!path.startsWith(rootPath)) {
          throw new IllegalArgumentException(path+" isn't under "+rootPath);
        }
        if (t.exclude(path.substring(rootPath.length()+1).replace(File.separatorChar, '/'))) {
          return true;
        }
        return false;
      } 
      
      private void buildMetrics(final Reckoner r, 
                                final IProgressMonitor mon, 
                                List<File> targets, Filter filter) {
        List<Metrics> metrics = r.computeMetrics(targets, mon);
        for(Metrics m : metrics) {
          if (filter.exclude(m)) {
            continue;
          }
          
          System.out.println(m.getPath()+": "+m.getLoc()+" LOC");
          MetricBuilder metric = generator.metric();
          metric.compilation(m.getClassName());
          if (m.getLoc() > Integer.MAX_VALUE) {
            reportError("#LOC too big to report: "+m.getClassName()+" - "+m.getLoc());
          }
          metric.linesOfCode((int) m.getLoc());
          metric.packageName(m.getPackageName());
          metric.build();
        }
      }
    };
  }
}

  