package com.surelogic.sierra.eclipse.teamserver.model;

public interface ITeamServerObserver {

	/**
	 * Notification that some state in the team server has changed.
	 * 
	 * @param server
	 *            the team server.
	 */
	void notify(TeamServer server);
}
