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
  private Throwable t;
  public IProgressMonitorWrapper(SLProgressMonitor m) {
    monitor = m;
  }
  
  public void beginTask(String name, int totalWork) {
    monitor.beginTask(name, totalWork);
  }

  public void done() {
    monitor.done();
  }

  public void error(String msg, Throwable t) {
    this.t = t;
  }
  
  public void error(String msg) {
  }
  
  public void failed(String msg, Throwable t) {
    monitor.done();
    this.t = t;
  }
  
  public void failed(String msg) {
    monitor.done();
  }

  public Throwable getFailureTrace() {
    return t;
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
