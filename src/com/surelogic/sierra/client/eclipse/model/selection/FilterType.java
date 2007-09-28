package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

public final class FilterType extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterType(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Type";
		}
	};

	FilterType(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "CLASS";
	}
}
