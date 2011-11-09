package com.surelogic.sierra.client.eclipse.model;

/**
 * Observes changes to sierra server connections in the client.
 */
public interface ISierraServerObserver {

	/**
	 * Clients of this method should never assume they are in a particular
	 * thread.
	 * 
	 * @param manager
	 *            the server manager.
	 */
	void notify(ConnectedServerManager manager);

}
