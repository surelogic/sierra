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

	private static final String DB_FIXED = "F";
	private static final String DB_NEW = "N";
	private static final String DB_UNCHANGED = "U";

	private static final String UI_FIXED = "Fixed";
	private static final String UI_NEW = "New";
	private static final String UI_UNCHANGED = "Unchanged";

	@Override
	protected String db2ui(String dbValue) {
		if (DB_NEW.equals(dbValue))
			return UI_NEW;
		else if (DB_FIXED.equals(dbValue))
			return UI_FIXED;
		else
			return UI_UNCHANGED;
	}

	@Override
	protected String ui2db(String uiValue) {
		if (UI_NEW.equals(uiValue))
			return DB_NEW;
		else if (UI_FIXED.equals(uiValue))
			return DB_FIXED;
		else
			return DB_UNCHANGED;
	}

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { UI_NEW, UI_UNCHANGED, UI_FIXED };
		f_allValues.addAll(Arrays.asList(values));
	}
}
