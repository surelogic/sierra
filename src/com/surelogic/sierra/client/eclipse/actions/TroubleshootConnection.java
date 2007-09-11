package com.surelogic.sierra.client.eclipse.actions;

import com.surelogic.sierra.client.eclipse.model.SierraServer;

public abstract class TroubleshootConnection {

	protected final SierraServer f_server;

	/**
	 * Constructs this object.
	 * 
	 * @param server
	 *            the mutable server configuration to be fixed.
	 */
	protected TroubleshootConnection(final SierraServer server) {
		if (server == null)
			throw new IllegalStateException("server must be non-null");
		f_server = server;
	}

	public final SierraServer getServer() {
		return f_server;
	}

	private boolean f_canceled = false;

	protected void setCanceled() {
		f_canceled = true;
	}

	/**
	 * Indicates that the user didn't try to fix the server location and
	 * authentication data.
	 * 
	 * @return <code>false</code> if the user fixed the server location and
	 *         authentication data, <code>false</code> otherwise.
	 */
	public final boolean isCanceled() {
		return f_canceled;
	}

	/**
	 * Tries to fix the server location and authentication data passed in to the
	 * constructor of this object.
	 * <p>
	 * Subclasses must override to take the appropriate UI actions to mutate the
	 * server object.
	 */
	abstract void fix();
}
