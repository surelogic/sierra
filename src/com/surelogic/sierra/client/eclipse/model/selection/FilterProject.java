package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collections;
import java.util.List;

import com.surelogic.sierra.client.eclipse.model.Projects;

public final class FilterProject extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterProject(selection, previous);
		}

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
		return "FO.PROJECT";
	}

	@Override
	protected void deriveAllValues() {
		final List<String> projectNames = Projects.getInstance()
				.getProjectNames();
		Collections.sort(projectNames);
		synchronized (this) {
			f_allValues.clear();
			f_allValues.addAll(projectNames);
		}
	}
}
