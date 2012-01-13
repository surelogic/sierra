package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;

public final class FilterSelection extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterSelection(selection, previous);
		}

		public String getFilterLabel() {
			return "Status";
		}
		
		@Override
		public boolean addWhereClauseIfUnusedFilter(Set<ISelectionFilterFactory> unused,
				                                    StringBuilder b, boolean first, 
				                                    boolean usesJoin) {
			first = addClausePrefix(b, first);
			addWhereClauseToFilterOutFixed(b, usesJoin);
			return first;
		}
	};

	FilterSelection(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	private static final String COLUMN_NAME = "STATUS";

	@Override
	protected String getColumnName() {
		return COLUMN_NAME;
	}

	private static final String FIXED = "Fixed";
	private static final String NEW = "New";
	private static final String UNCHANGED = "Unchanged";

	@Override
	protected void deriveAllValues() {
		synchronized (this) {
			f_allValues.clear();
			f_allValues.add(NEW);
			f_allValues.add(UNCHANGED);
			f_allValues.add(FIXED);
		}
	}

	static void addWhereClauseToFilterOutFixed(StringBuilder b, boolean usesJoin) {
		if (usesJoin) {
			b.append(getTablePrefix(true));
			b.append(COLUMN_NAME + " != '" + FIXED + "'");
			b.append(" AND ");
		}
		b.append(getTablePrefix(false));
		b.append(COLUMN_NAME + " != '" + FIXED + "'");
	}
}