package com.surelogic.sierra.client.eclipse.model;

/**
 * A trivial, take no action, implementation of an {@link IDatabaseObserver}.
 */
public abstract class AbstractDatabaseObserver extends AbstractUpdatable
implements IDatabaseObserver {

	@Override
  public void changed() {
		// Do nothing
	}

	@Override
  public void projectDeleted() {
		// Do nothing
	}

	@Override
  public void scanLoaded() {
		// Do nothing
	}

	@Override
  public void serverSynchronized() {
		// Do nothing
	}
	
	@Override
  public void projectSynchronized() {
		// Do nothing
	}

	@Override
  public void findingMutated() {
		// Do nothing
	}

	@Override
  public void databaseDeleted() {
		// Do nothing
	}
}
