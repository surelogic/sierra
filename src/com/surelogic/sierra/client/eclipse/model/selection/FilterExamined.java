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

	private static final String DB_YES = "Y";
	private static final String DB_NO = "N";

	private static final String UI_YES = "Yes";
	private static final String UI_NO = "No";

	@Override
	protected String db2ui(String dbValue) {
		if (DB_YES.equals(dbValue))
			return UI_YES;
		else
			return UI_NO;
	}

	@Override
	protected String ui2db(String uiValue) {
		if (UI_YES.equals(uiValue))
			return DB_YES;
		else
			return DB_NO;
	}

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { UI_YES, UI_NO };
		f_allValues.addAll(Arrays.asList(values));
	}
}
