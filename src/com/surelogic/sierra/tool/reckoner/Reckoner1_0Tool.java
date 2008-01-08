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
  
  public IToolInstance create(final ArtifactGenerator generator, final SLProgressMonitor monitor) {
    return new AbstractToolInstance(this, generator, monitor) {
      @Override
      protected void execute() throws Exception {              
        List<File> targets = new ArrayList<File>();
        for(IToolTarget t : getSrcTargets()) {
          targets.add(new File(t.getLocation()));
        }
          
        Reckoner r = new Reckoner();
        IProgressMonitor mon = new IProgressMonitorWrapper(monitor);
        List<Metrics> metrics = r.computeMetrics(targets, mon);
        for(Metrics m : metrics) {
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

  