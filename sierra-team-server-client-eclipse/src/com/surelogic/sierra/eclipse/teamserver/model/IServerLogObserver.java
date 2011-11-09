package com.surelogic.sierra.eclipse.teamserver.model;

public interface IServerLogObserver {

	/**
	 * Notification that the log contents have changed.
	 * <p>
	 * The thread context that this call is invoked from is not guaranteed to be
	 * consistent. It may change from call to call.
	 * 
	 * @param log
	 *            the log.
	 */
	void notify(ServerLog log);
}
