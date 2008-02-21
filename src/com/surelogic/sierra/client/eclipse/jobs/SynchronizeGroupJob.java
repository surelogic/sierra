/*
 * Created on Feb 21, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A Job designed to coordinate SynchronizeJobs
 * 
 * @author Edwin
 */
public class SynchronizeGroupJob extends Job {
  public SynchronizeGroupJob() {
    super("Waiting for synchronize jobs");
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    // TODO Auto-generated method stub
    return null;
  }
}
