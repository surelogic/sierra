package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;

public final class FilterAudited extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterAudited(selection, previous);
		}

		public String getFilterLabel() {
			return "Audited";
		}
	};

	FilterAudited(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "AUDITED";
	}

	private static final String YES = "Yes";
	private static final String NO = "No";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { YES, NO };
		synchronized (this) {
			f_allValues.clear();
			f_allValues.addAll(Arrays.asList(values));
		}
	}
}
