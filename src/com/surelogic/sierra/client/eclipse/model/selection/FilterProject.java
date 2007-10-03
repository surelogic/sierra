package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterProject extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterProject(selection, previous);
		}

		public String getFilterLabel() {
			return "Project";
		}
	};

	FilterProject(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "PROJECT";
	}
}
