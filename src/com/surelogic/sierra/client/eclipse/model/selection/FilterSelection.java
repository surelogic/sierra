package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;
import java.util.concurrent.Executor;

public final class FilterSelection extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterSelection(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Status";
		}
	};

	FilterSelection(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	private static final String COLUMN_NAME = "STATE";

	@Override
	protected String getColumnName() {
		return COLUMN_NAME;
	}

	private static final String FIXED = "Fixed";
	private static final String NEW = "New";
	private static final String UNCHANGED = "Unchanged";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { NEW, UNCHANGED, FIXED };
		f_allValues.addAll(Arrays.asList(values));
	}

	protected static void addWhereClauseToFilterOutFixed(StringBuilder b) {
		b.append(COLUMN_NAME + " != '" + FIXED + "' ");
	}
}
