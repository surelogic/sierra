package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;

public class ProjectView extends BlockPanel {

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Project");

	}

	public void setSelection(Project project) {
		if (project == null) {
			setSummary("Select a project");
		} else {
			setSummary(project.getName());
		}

	}

}
