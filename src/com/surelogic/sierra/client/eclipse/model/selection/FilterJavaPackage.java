package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;

public final class FilterJavaPackage extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterJavaPackage(selection, previous);
		}

		public String getFilterLabel() {
			return "Java Package";
		}
	};

	FilterJavaPackage(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "PACKAGE";
	}

	@Override
	public Image getImageFor(String value) {
		return SLImages.getImage(CommonImages.IMG_PROJECT);
	}
}
