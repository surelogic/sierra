package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

public final class FilterFindingType extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterFindingType(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Finding Type";
		}
	};

	FilterFindingType(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}
	
	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "FINDING_TYPE";
	}

}
