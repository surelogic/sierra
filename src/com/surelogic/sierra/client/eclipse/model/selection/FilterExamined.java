package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;
import java.util.concurrent.Executor;

public final class FilterExamined extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterExamined(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Examined";
		}
	};

	FilterExamined(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "EXAMINED";
	}

	private static final String YES = "Y";
	private static final String NO = "N";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { YES, NO };
		f_allValues.addAll(Arrays.asList(values));
	}
}
