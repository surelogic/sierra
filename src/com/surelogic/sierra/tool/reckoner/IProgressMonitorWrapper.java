package com.surelogic.sierra.tool.reckoner;

import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.SLProgressMonitor;

/**
 * Copied here from common-eclipse, due to jar versioning differences
 * @author Edwin.Chan
 *
 */
public class IProgressMonitorWrapper implements SLProgressMonitor, IProgressMonitor {
  private final SLProgressMonitor monitor;
  
  public IProgressMonitorWrapper(SLProgressMonitor m) {
    monitor = m;
  }
  
  public void beginTask(String name, int totalWork) {
    monitor.beginTask(name, totalWork);
  }

  public void done() {
    monitor.done();
  }

  public void failed(Throwable t) {
    
  }

  public Throwable getFailureTrace() {
    return null;
  }

  public void internalWorked(double work) {
    monitor.internalWorked(work);
  }

  public boolean isCanceled() {
    return monitor.isCanceled();
  }

  public void setCanceled(boolean value) {
    monitor.setCanceled(value);
  }

  public void setTaskName(String name) {
    monitor.setTaskName(name);
  }

  public void subTask(String name) {
    monitor.subTask(name);
  }

  public void worked(int work) {
    monitor.worked(work);
  }
}
