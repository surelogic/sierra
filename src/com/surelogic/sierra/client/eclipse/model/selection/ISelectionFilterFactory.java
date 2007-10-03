package com.surelogic.sierra.client.eclipse.model.selection;

public interface ISelectionFilterFactory extends
		Comparable<ISelectionFilterFactory> {

	Filter construct(final Selection selection, final Filter previous);

	String getFilterLabel();
}
