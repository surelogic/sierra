package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.sierra.metrics.Reckoner;
import com.surelogic.sierra.metrics.model.Metrics;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MetricBuilder;

public class Reckoner1_0Tool extends AbstractTool {
  /*
  private static final String[] required = {
	  SierraToolConstants.JDT_CORE_PLUGIN_ID,
	  SierraToolConstants.CORE_RUNTIME_PLUGIN_ID
  };
  */
	
  public Reckoner1_0Tool(ReckonerFactory f, Config config) {
    super(f, config);
  }

  public Set<ArtifactType> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  @Override
  public List<File> getRequiredJars() {
	  final List<File> jars = new ArrayList<File>();	
	  findJars(jars, new File(config.getToolsDirectory(), "reckoner/lib"));
	  jars.add(new File(config.getToolsDirectory(), "reckoner/reckoner.jar"));
	  
	  // Add all the plugins needed by Reckoner (e.g. JDT Core and
	  // company)
	  for (String id : config.getPluginDirs().keySet()) {
		  if (id.startsWith("org.eclipse")) {
			  addPluginToPath(debug, jars, id);
		  } else {
			  //System.out.println("Unused: "+id);
		  }
	  }
	  /*
	  for (String id : required) {
		  // FIX what about transitive dependencies?
		  addPluginToPath(debug, jars, id);
	  }
	  */
	  return jars;
  }
  
	protected IToolInstance create(String name, final ILazyArtifactGenerator generator,
			boolean close) {
    return new AbstractToolInstance(debug, this, generator, close) {
      @Override
			protected SLStatus execute(SLProgressMonitor monitor)
					throws Exception {
    	final boolean debug = LOG.isLoggable(Level.FINE);
        final Reckoner r = new Reckoner();

        final List<File> targets = new ArrayList<File>();
        //monitor.beginTask("Reckoner setup", IProgressMonitor.UNKNOWN);
		prepJavaFiles(new SourcePrep() {
			public void prep(File f) {
				targets.add(f);
			}					
		});
        
        final Set<String> processed = new HashSet<String>();
        final int size = targets.size();
        if (size <= 0) {
        	monitor.begin();
        } else {
        	monitor.begin(size);
        }
        for(File f : targets) {
        	final String path = f.getPath();
        	monitor.subTask("Building metrics for "+path);
            try {
            	Metrics m = r.countLOC(f);
            	if (m == null) {
							status.addChild(SLStatus.createWarningStatus(-1,
									"Problem reading " + path));
            		continue;
            	}
            	if (debug) {
							System.out.println(m.getPath() + ": " + m.getLoc()
									+ " LOC");
            	}
						final String key = m.getPackageName() + ','
								+ m.getClassName();
        		if (processed.contains(key)) {
							status.addChild(SLStatus.createWarningStatus(-1,
									"Duplicate metric found for " + key));
        		}
            	
            	buildMetrics(m);
            	monitor.worked(1);
            } catch (Exception e) {
						status.addChild(SLStatus.createWarningStatus(-1,
								"Unexpected problem with " + path, e));
            }
        }
        return status.build();
      }

      private void buildMetrics(Metrics m) {
    	  MetricBuilder metric = getGenerator().metric();
    	  metric.compilation(m.getClassName());
    	  if (m.getLoc() > Integer.MAX_VALUE) {
					reportError("#LOC too big to report: " + m.getClassName()
							+ " - " + m.getLoc());
    	  }
    	  metric.linesOfCode((int) m.getLoc());          
    	  metric.packageName(m.getPackageName());
    	  metric.build();
      }
    };
  }
}

  