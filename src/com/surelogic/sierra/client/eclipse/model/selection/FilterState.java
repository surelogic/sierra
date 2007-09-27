package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;
import java.util.concurrent.Executor;

public final class FilterState extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterState(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "State";
		}
	};

	FilterState(Selection selection, Filter previous, Executor executor) {
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

	private static final String FIXED = "F";
	private static final String NEW = "N";
	private static final String UNCHANGED = "U";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { NEW, UNCHANGED, FIXED };
		f_allValues.addAll(Arrays.asList(values));
	}

	protected static void addWhereClauseToFilterOutFixed(StringBuilder b) {
		b.append(COLUMN_NAME + " != '" + FIXED + "' ");
	}
}
