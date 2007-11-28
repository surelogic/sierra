package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;

public final class FilterFindingCategory extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterFindingCategory(selection, previous);
		}

		public String getFilterLabel() {
			return "Finding Category";
		}
	};

	FilterFindingCategory(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "CATEGORY";
	}

	@Override
	public Image getImageFor(String value) {
		return SLImages.getImage(SLImages.IMG_CATEGORY);
	}
}
