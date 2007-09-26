package com.surelogic.sierra.client.eclipse.model.selection;

public abstract class AbstractFilterFactory implements ISelectionFilterFactory {

	public int compareTo(ISelectionFilterFactory o) {
		return getFilterLabel().compareTo(o.getFilterLabel());
	}

	@Override
	public String toString() {
		return getFilterLabel();
	}
}
