package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class ProjectView extends BlockPanel {

	private final VerticalPanel chart = new VerticalPanel();

	@Override
	protected void onInitialize(VerticalPanel contentPanel) {
		setTitle("Project");
		contentPanel.add(chart);
	}

	public void setSelection(Project project) {
		if (project == null) {
			setSummary("Select a project");
		} else {
			setSummary(project.getName());
			chart.clear();
			chart.add(ChartBuilder.name("ProjectFindingsChart").width(800)
					.prop("projectName", project.getName()).build());
			chart.add(ChartBuilder.name("ProjectCompilationsChart").width(800)
					.prop("projectName", project.getName()).build());
		}

	}
}
