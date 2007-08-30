package com.surelogic.sierra.client.eclipse.model;

public interface IProjectObserver {

	/**
	 * Notifies an observer that a project has been added to or deleted from the
	 * database.
	 * 
	 * @param p
	 *            the project manager object.
	 */
	void notify(final Project p);
}
