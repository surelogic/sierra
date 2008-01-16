package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Logger;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;

public class NewScanJob extends Job {
  /** The logger */
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  
  final Config config;
  final DatabaseJob afterJob;
  
  NewScanJob(String name, Config cfg, DatabaseJob after) {
    super(name);
    config = cfg;
    afterJob = after;
  }
  
  protected void runTools(final SLProgressMonitor mon) {
    System.out.println("Excluded: "+config.getExcludedToolsList());
  
    final ITool t = ToolUtil.create(config);                           
    System.out.println("Java version: "+config.getJavaVersion());
    System.out.println("Rules file: "+config.getPmdRulesFile());
  
    IToolInstance ti = t.create(config, mon);                         
    ti.run();
  }
  
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
    try {            
      runTools(wrapper);
      if (afterJob != null) {
        afterJob.schedule();
      }
    } catch(Throwable ex) {
      if (monitor.isCanceled()) {
        wrapper.failed("Caught exception in run() while "+getName(), ex);
      }
    }
    if (wrapper.getFailureTrace() != null && !monitor.isCanceled()) {
      Throwable ex = wrapper.getFailureTrace();
      LOG.severe(ex.getMessage());
      return SLStatus.createErrorStatus("Failed "+getName(), ex);
    }
    return Status.OK_STATUS;
  }
}
