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
    setPriority(Job.DECORATE);
    afterJob.setPriority(Job.DECORATE);
  }
  
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    LOG.info(this.getName());
    final SLProgressMonitor wrapper = new SLProgressMonitorWrapper(monitor);
    try {            
      ToolUtil.scan(config, wrapper, true);
      
      if (afterJob != null) {
        afterJob.schedule();
      }
    } catch(Throwable ex) {
      if (!monitor.isCanceled()) {
        wrapper.failed("Caught exception while "+getName(), ex);
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
