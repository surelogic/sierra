package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterAuditCount extends FilterNumberValue {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		@Override
    public Filter construct(Selection selection, Filter previous) {
			return new FilterAuditCount(selection, previous);
		}

		@Override
    public String getFilterLabel() {
			return "Audits";
		}
	};

	FilterAuditCount(Selection selection, Filter previous) {
		super(selection, previous);
		f_quote = false;
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "AUDIT_COUNT";
	}

}
