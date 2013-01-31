package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterTool extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		@Override
    public Filter construct(Selection selection, Filter previous) {
			return new FilterTool(selection, previous);
		}

		@Override
    public String getFilterLabel() {
			return "Tool";
		}
	};

	FilterTool(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "TOOL";
	}
}
