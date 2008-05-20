package com.surelogic.sierra.client.eclipse.model;

public interface IBuglinkDataObserver {
	/**
	 * Notifies an observer that Buglink data has been modified in the
	 * database.
	 * <p>
	 * Note that this call is <b>not</b> be made from a UI thread.
	 */
	void notify(BuglinkData bd);
}
