package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Set;

public final class FilterVerificationStatus extends Filter {
	private static final String COLUMN_NAME = "ASSURANCE_TYPE";
	public static final String CONSISTENT = "C";
	public static final String INCONSISTENT = "I";
	
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterVerificationStatus(selection, previous);
		}

		public String getFilterLabel() {
			return "Verification Status";
		}
		
		@Override
		public boolean addWhereClauseIfUnusedFilter(Set<ISelectionFilterFactory> unused,
				                                    StringBuilder b, boolean first) {
			if (unused.contains(FilterResultType.FACTORY)) { // both unused
				first = addClausePrefix(b, first);
				b.append(COLUMN_NAME + " is NULL");
			}
			return first;
		}
	};

	FilterVerificationStatus(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}
	
	@Override
	protected String getColumnName() {
		return COLUMN_NAME;
	}
	
	@Override
	protected void deriveAllValues() {
		synchronized (this) {
			f_allValues.clear();
			f_allValues.add(CONSISTENT);
			f_allValues.add(INCONSISTENT);
			f_allValues.add(null);
		}
	}
	
	@Override
	public String getLabel(String initial) {
		if (CONSISTENT.equals(initial)) {
			return "Consistent";
		}
		if (INCONSISTENT.equals(initial)) {
			return "Inconsistent";
		}
		return "N/A";
	}	
}
