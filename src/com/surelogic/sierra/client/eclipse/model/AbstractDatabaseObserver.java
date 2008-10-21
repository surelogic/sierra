package com.surelogic.sierra.client.eclipse.model;

public abstract class AbstractDatabaseObserver implements IDatabaseObserver {

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
