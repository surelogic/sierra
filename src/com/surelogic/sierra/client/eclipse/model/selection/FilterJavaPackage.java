package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;

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
		return "FO.PACKAGE";
	}

	@Override
	public Image getImageFor(String value) {
		return SLImages.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE);
	}
}
