package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
  
  protected IToolInstance create(final ArtifactGenerator generator, 
      final SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {     
    	final boolean debug = LOG.isLoggable(Level.FINE);
        final Reckoner r = new Reckoner();

        final List<File> targets = new ArrayList<File>();
        monitor.beginTask("Reckoner setup", IProgressMonitor.UNKNOWN);
        for(IToolTarget t : getSrcTargets()) {
            for(URI loc : t.getFiles()) {
                File f = new File(loc);
                if (f.exists()) {      
                	targets.add(f);
                }
            }
        }
        
        monitor.beginTask("Reckoner", targets.size());
        for(File f : targets) {
        	final String path = f.getPath();
        	monitor.subTask("Building metrics for "+path);
            try {
            	Metrics m = r.countLOC(f);
            	if (m == null) {
            		monitor.error("Problem reading "+path);
            		continue;
            	}
            	if (debug) {
            		System.out.println(m.getPath()+": "+m.getLoc()+" LOC");
            	}
            	buildMetrics(m);
            	monitor.worked(1);
            } catch (Exception e) {
            	monitor.error("Unexpected problem with "+path, e);
            }
        }
      }

      private void buildMetrics(Metrics m) {
    	  MetricBuilder metric = generator.metric();
    	  metric.compilation(m.getClassName());
    	  if (m.getLoc() > Integer.MAX_VALUE) {
    		  reportError("#LOC too big to report: "+m.getClassName()+" - "+m.getLoc());
    	  }
    	  metric.linesOfCode((int) m.getLoc());          
    	  metric.packageName(m.getPackageName());
    	  metric.build();
      }
    };
  }
}

  