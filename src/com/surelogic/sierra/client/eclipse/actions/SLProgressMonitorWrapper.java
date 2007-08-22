package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.surelogic.common.SLProgressMonitor;

/**
 * The wrapper for SLProgressMonitor. It refers to
 * org.eclipse.core.runtime.ProgressMonitorWrapper.
 * 
 * @author Tanmay.Sinha
 * 
 */
public class SLProgressMonitorWrapper implements SLProgressMonitor {

	/** The wrapped progress monitor. */
	private IProgressMonitor progressMonitor;

	/**
	 * Creates a new wrapper around the given monitor.
	 * 
	 * @param monitor
	 *            the progress monitor to forward to
	 */
	protected SLProgressMonitorWrapper(IProgressMonitor monitor) {
		Assert.isNotNull(monitor);
		progressMonitor = monitor;
	}

	public void beginTask(String name, int totalWork) {
		progressMonitor.beginTask(name, totalWork);

	}

	public void done() {
		progressMonitor.done();

	}

	public void internalWorked(double work) {
		progressMonitor.internalWorked(work);

	}

	public boolean isCanceled() {
		return progressMonitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		progressMonitor.setCanceled(value);

	}

	public void setTaskName(String name) {
		progressMonitor.setTaskName(name);

	}

	public void subTask(String name) {
		progressMonitor.subTask(name);

	}

	public void worked(int work) {
		progressMonitor.worked(work);
	}

}
