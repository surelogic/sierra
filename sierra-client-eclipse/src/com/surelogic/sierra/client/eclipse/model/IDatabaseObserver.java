package com.surelogic.sierra.client.eclipse.model;

public interface IDatabaseObserver {

	/**
	 * Notifies that something has changed in the database. This method is
	 * called after the more specific notification, e.g., {@link #scanLoaded()},
	 * has been invoked.
	 */
	void changed();

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
	 * Notification that the Sierra database has been removed from the file
	 * system.
	 */
	void databaseDeleted();

	/**
	 * Notification that the client database has been synchronized with a Buglink
	 * server.
	 */
	void serverSynchronized();

	/**
	 * Notification that the client database has been synchronized with a team
	 * server.
	 */
	void projectSynchronized();
	
	/**
	 * Notification that a finding has been mutated.
	 */
	void findingMutated();
}
