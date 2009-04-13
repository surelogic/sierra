package com.surelogic.sierra.tool.reckoner;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.sierra.metrics.Reckoner;
import com.surelogic.sierra.metrics.model.Metrics;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MetricBuilder;

public class Reckoner1_0Tool extends AbstractToolInstance {
	/*
  private static final String[] required = {
	  SierraToolConstants.JDT_CORE_PLUGIN_ID,
	  SierraToolConstants.CORE_RUNTIME_PLUGIN_ID
  };
	 */

	public Reckoner1_0Tool(ReckonerFactory f, Config config, ILazyArtifactGenerator generator, boolean close) {
		super(f, config, generator, close);
	}

	@Override
	protected SLStatus execute(SLProgressMonitor monitor) throws Exception {
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
}

  