package com.surelogic.sierra.client.eclipse.model.selection;

public final class FilterCommentCount extends FilterNumberValue {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterCommentCount(selection, previous);
		}

		public String getFilterLabel() {
			return "Comment Count";
		}
	};

	FilterCommentCount(Selection selection, Filter previous) {
		super(selection, previous);
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
