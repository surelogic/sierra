package com.surelogic.sierra.eclipse.teamserver.model;

public interface ITeamServerObserver {

	/**
	 * Notification that the running state of the local team server has changed.
	 * <p>
	 * The thread context that this call is invoked from is not guaranteed to be
	 * consistent. It may change from call to call.
	 * 
	 * @param server
	 *            the team server.
	 */
	void notify(TeamServer server);

	/**
	 * Notification that the startup of the team server process failed before
	 * the server got up and running.
	 * <p>
	 * The thread context that this call is invoked from is not guaranteed to be
	 * consistent. It may change from call to call.
	 * 
	 * @param server
	 *            the team server.
	 */
	void notifyStartupFailure(TeamServer server);
}
