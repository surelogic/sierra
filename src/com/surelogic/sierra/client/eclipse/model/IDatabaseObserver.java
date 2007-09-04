package com.surelogic.sierra.client.eclipse.model;

public interface IDatabaseObserver {

	/**
	 * Notification that a scan has been loaded into the client database.
	 */
	void scanLoaded();

	/**
	 * Notification that all Sierra data about a particular project has been
	 * deleted from the client database.
	 */
	void projectDeleted();

	/**
	 * Notification that the client database has been synchronized with a Sierra
	 * server.
	 */
	void serverSynchronized();
}
