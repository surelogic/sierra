package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.model.Projects;

public final class FilterProject extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		@Override
    public Filter construct(Selection selection, Filter previous) {
			return new FilterProject(selection, previous);
		}

		@Override
    public String getFilterLabel() {
			return "Project";
		}
	};

	FilterProject(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "PROJECT";
	}

	@Override
	public Image getImageFor(String value) {
		return SLImages.getImageForProject(value);
	}

	@Override
	protected void deriveAllValues() {
		final List<String> projectNames = Projects.getInstance()
				.getProjectNames();
		Collections.sort(projectNames, String.CASE_INSENSITIVE_ORDER);
		synchronized (this) {
			f_allValues.clear();
			f_allValues.addAll(projectNames);
		}
	}
}
