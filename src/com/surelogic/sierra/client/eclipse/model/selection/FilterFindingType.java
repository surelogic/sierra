package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterFindingType extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterFindingType(selection, previous);
		}

		public String getFilterLabel() {
			return "Finding Type";
		}
	};

	FilterFindingType(Selection selection, Filter previous) {
		super(selection, previous);
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
