package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

public final class FilterProject extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterProject(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Project";
		}
	};

	FilterProject(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
	}

	@Override
	protected String getColumnName() {
		return "PROJECT";
	}
}
