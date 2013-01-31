package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;

public final class FilterJavaClass extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		@Override
    public Filter construct(Selection selection, Filter previous) {
			return new FilterJavaClass(selection, previous);
		}

		@Override
    public String getFilterLabel() {
			return "Java Class";
		}
	};

	FilterJavaClass(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "CLASS";
	}

	@Override
	public Image getImageFor(String value) {
		return SLImages.getImage(CommonImages.IMG_CLASS);
	}
}
