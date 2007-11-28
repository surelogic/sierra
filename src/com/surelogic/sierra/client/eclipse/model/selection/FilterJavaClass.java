package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterJavaClass extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(FindingSearch selection, Filter previous) {
			return new FilterJavaClass(selection, previous);
		}

		public String getFilterLabel() {
			return "Java Class";
		}
	};

	FilterJavaClass(FindingSearch selection, Filter previous) {
		super(selection, previous);
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
