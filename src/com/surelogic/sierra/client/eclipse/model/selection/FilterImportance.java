package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;
import java.util.concurrent.Executor;

public final class FilterImportance extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterImportance(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Importance";
		}
	};

	FilterImportance(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "IMPORTANCE";
	}

	public static final String CRITICAL = "Critical";
	public static final String HIGH = "High";
	public static final String MEDIUM = "Medium";
	public static final String LOW = "Low";
	public static final String IRRELEVANT = "Irrelevant";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { CRITICAL, HIGH, MEDIUM, LOW,
				IRRELEVANT };
		f_allValues.addAll(Arrays.asList(values));
	}
}
