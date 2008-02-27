package com.surelogic.sierra.eclipse.teamserver.model;

public interface ITeamServerObserver {

	/**
	 * Notification that some state in the team server has changed.
	 * <p>
	 * The thread context that this call is invoked from is not guaranteed to be
	 * consistent. It may change from call to call.
	 * 
	 * @param server
	 *            the team server.
	 */
	void notify(TeamServer server);
}
