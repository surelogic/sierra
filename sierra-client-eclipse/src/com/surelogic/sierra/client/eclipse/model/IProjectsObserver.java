package com.surelogic.sierra.client.eclipse.model;

public interface IProjectsObserver {

	/**
	 * Notifies an observer that a project has been added to or deleted from the
	 * database.
	 * <p>
	 * Note that this call is <b>not</b> be made from a UI thread.
	 * 
	 * @param p
	 *            the project manager object.
	 */
	void notify(Projects p);
}
