package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

public final class FilterCommentCount extends FilterNumberValue {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterCommentCount(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Comment Count";
		}
	};

	FilterCommentCount(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
		f_quote = false;
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "COMMENT_COUNT";
	}

}
