package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.concurrent.Executor;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;

public final class FilterPackage extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous,
				Executor executor) {
			return new FilterPackage(selection, previous, executor);
		}

		public String getFilterLabel() {
			return "Package";
		}
	};

	FilterPackage(Selection selection, Filter previous, Executor executor) {
		super(selection, previous, executor);
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
		return SLImages.getJDTImage(ISharedImages.IMG_OBJS_PACKAGE);
	}
}
