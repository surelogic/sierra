package com.surelogic.sierra.client.eclipse.model;

/**
 * A trivial, take no action, implementation of an {@link IDatabaseObserver}.
 */
public abstract class AbstractDatabaseObserver extends AbstractUpdatable
implements IDatabaseObserver {

	public void changed() {
		// Do nothing
	}

	public void projectDeleted() {
		// Do nothing
	}

	public void scanLoaded() {
		// Do nothing
	}

	public void serverSynchronized() {
		// Do nothing
	}

	public void findingMutated() {
		// Do nothing
	}

	public void databaseDeleted() {
		// Do nothing
	}
}
