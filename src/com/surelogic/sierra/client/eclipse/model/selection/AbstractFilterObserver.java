package com.surelogic.sierra.client.eclipse.model.selection;

public abstract class AbstractFilterObserver implements IFilterObserver {

	public void porous(Filter filter) {
		// do nothing
	}

	public void contentsChanged(Filter filter) {
		// do nothing
	}

	public void contentsEmpty(Filter filter) {
		// do nothing
	}

	public void dispose(Filter filter) {
		// do nothing
	}

	public void queryFailure(Filter filter, Exception e) {
		// do nothing
	}
}
