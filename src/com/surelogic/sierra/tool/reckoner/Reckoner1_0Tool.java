package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.metrics.Reckoner;
import com.surelogic.sierra.metrics.model.Metrics;
import com.surelogic.sierra.tool.AbstractTool;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.MetricBuilder;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class Reckoner1_0Tool extends AbstractTool {
  public Reckoner1_0Tool(boolean debug) {
    super("Reckoner", "1.0", "Reckoner", "", debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  protected IToolInstance create(final ArtifactGenerator generator, boolean close) {
    return new AbstractToolInstance(debug, this, generator, close) {
      @Override
      protected SLStatus execute(SLProgressMonitor monitor) throws Exception {     
    	final boolean debug = LOG.isLoggable(Level.FINE);
        final Reckoner r = new Reckoner();

        final List<File> targets = new ArrayList<File>();
        monitor.beginTask("Reckoner setup", IProgressMonitor.UNKNOWN);
        for(IToolTarget t : getSrcTargets()) {
            for(URI loc : t.getFiles()) {
                File f = new File(loc);
                if (f.exists() && f.getName().endsWith(".java")) { 
                	targets.add(f);
                }
            }
        }
        
        final Set<String> processed = new HashSet<String>();
        monitor.beginTask("Reckoner", targets.size());
        for(File f : targets) {
        	final String path = f.getPath();
        	monitor.subTask("Building metrics for "+path);
            try {
            	Metrics m = r.countLOC(f);
            	if (m == null) {
            		status.add(SLStatus.createWarningStatus(-1, "Problem reading "+path));
            		continue;
            	}
            	if (debug) {
            		System.out.println(m.getPath()+": "+m.getLoc()+" LOC");
            	}
        		final String key = m.getPackageName()+','+m.getClassName();
        		if (processed.contains(key)) {
        			status.add(SLStatus.createWarningStatus(-1, "Duplicate metric found for "+key));
        		}
            	
            	buildMetrics(m);
            	monitor.worked(1);
            } catch (Exception e) {
            	status.add(SLStatus.createWarningStatus(-1, "Unexpected problem with "+path, e));
            }
        }
        return status.build();
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

  