package com.surelogic.sierra.jdbc;

import com.surelogic.common.SLProgressMonitor;

public class EmptyProgressMonitor implements SLProgressMonitor {

	private static final EmptyProgressMonitor singleton = new EmptyProgressMonitor();

	public void beginTask(String name, int totalWork) {
		// Do nothing

	}

	public void done() {
		// Do nothing

	}

	public void internalWorked(double work) {
		// Do nothing

	}

	public boolean isCanceled() {
		// Do nothing
		return false;
	}

	public void setCanceled(boolean value) {
		// Do nothing

	}

	public void setTaskName(String name) {
		// Do nothing

	}

	public void subTask(String name) {
		// Do nothing

	}

	public void worked(int work) {
		// Do nothing
	}

	public static SLProgressMonitor instance() {
		return singleton;
	}

  public void failed(Throwable t) {
  }

  public Throwable getFailureTrace() {
    return null;
  }
}
