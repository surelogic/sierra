package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

public interface ISelectionFilterFactory extends
		Comparable<ISelectionFilterFactory> {

	Filter construct(final Selection selection, final Filter previous,
			final Executor executor);

	String getFilterLabel();
}
