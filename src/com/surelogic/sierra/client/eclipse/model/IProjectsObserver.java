package com.surelogic.sierra.client.eclipse.model;

public interface IProjectsObserver {

	/**
	 * Notifies an observer that a project has been added to or deleted from the
	 * database.
	 * 
	 * @param p
	 *            the project manager object.
	 */
	void notify(final Projects p);
}
