package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.ui.BlockPanel;
import com.surelogic.sierra.gwt.client.util.ChartBuilder;

public class ProjectView extends BlockPanel {

	private final HorizontalPanel chart = new HorizontalPanel();

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
			chart.add(ChartBuilder.name("ProjectFindingsChart").prop(
					"projectName", project.getName()).build());
			chart.add(ChartBuilder.name("ProjectCompilationsChart").prop(
					"projectName", project.getName()).build());
		}

	}
}
