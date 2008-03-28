package com.surelogic.sierra.client.eclipse.actions;

import com.surelogic.sierra.client.eclipse.model.SierraServer;

public abstract class TroubleshootConnection {

	protected final SierraServer f_server;

	protected final String f_projectName;

	/**
	 * Constructs this object.
	 * 
	 * @param server
	 *            the mutable server configuration to be fixed.
	 * @param projectName
	 *            the project name, or <code>null</code> if no project or it
	 *            is unknown.
	 */
	protected TroubleshootConnection(final SierraServer server,
			final String projectName) {
		if (server == null)
			throw new IllegalStateException("server must be non-null");
		f_server = server;
		if (projectName != null)
			f_projectName = projectName;
		else
			f_projectName = "(unknown)";
	}

	public final SierraServer getServer() {
		return f_server;
	}
	
	public final String getProjectName() {
		return f_projectName;
	}
	
	private boolean f_retry = true;

	protected void setRetry(boolean retry) {
		f_retry = retry;
	}

	/**
	 * Indicates if the troubleshooting object wants the job to retry the action
	 * that was troubleshooted.
	 * 
	 * @return <code>true</code> if a retry should be attempted,
	 *         <code>false</code> otherwise.
	 */
	public final boolean retry() {
		return f_retry;
	}

	/**
	 * Tries to fix the server location and authentication data passed in to the
	 * constructor of this object.
	 * <p>
	 * Subclasses must override to take the appropriate UI actions to mutate the
	 * server object.
	 */
	public abstract void fix();

	/**	
	 * @return true if the server should be considered failed
	 */
	public boolean failServer() {
		return !f_retry;
	}
}
