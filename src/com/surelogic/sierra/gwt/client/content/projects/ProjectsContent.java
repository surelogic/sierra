package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Project;

public class ProjectsContent extends
		ListContentComposite<Project, ProjectCache> {
	private static final ProjectsContent instance = new ProjectsContent();

	public static ProjectsContent getInstance() {
		return instance;
	}

	private ProjectsContent() {
		super(new ProjectCache());
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Projects");

		// TODO Auto-generated method stub
	}

	@Override
	protected String getItemText(Project item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(Project item, String query) {
		return item.getName().toLowerCase().contains(query.toLowerCase());
	}

	@Override
	protected void onSelectionChanged(Project item) {
		// TODO Auto-generated method stub

	}
}
