package com.surelogic.sierra.client.eclipse.model;

/**
 * Observes changes to BugLink data in the client.
 */
public interface IBuglinkDataObserver {
	/**
	 * Notifies an observer that BugLink data has been modified in the database.
	 * <p>
	 * Note that this call may not be made from a UI thread.
	 */
	void notify(BuglinkData bd);
}
