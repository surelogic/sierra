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

	@Override
	protected String getColumnName() {
		return "STATE";
	}

	private static final String FIXED = "Fixed";
	private static final String NEW = "New";
	private static final String UNCHANGED = "Unchanged";

	@Override
	protected String valueMapper(String dbValue) {
		if ("N".equals(dbValue))
			return NEW;
		else if ("F".equals(dbValue))
			return FIXED;
		else
			return UNCHANGED;
	}

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { NEW, UNCHANGED, FIXED };
		f_allValues.addAll(Arrays.asList(values));
	}
}
