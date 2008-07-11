package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.ListContentComposite;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.cache.ProjectCache;
import com.surelogic.sierra.gwt.client.data.cache.ScanFilterCache;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class ProjectsContent extends
		ListContentComposite<Project, ProjectCache> {
	private static final ProjectsContent instance = new ProjectsContent();
	private final ProjectView projectView = new ProjectView();

	public static ProjectsContent getInstance() {
		return instance;
	}

	private ProjectsContent() {
		super(ProjectCache.getInstance());
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel,
			VerticalPanel selectionPanel) {
		setCaption("Projects");

		projectView.initialize();
		selectionPanel.add(projectView);
	}

	@Override
	protected String getItemText(Project item) {
		return item.getName();
	}

	@Override
	protected boolean isMatch(Project item, String query) {
		return LangUtil.containsIgnoreCase(item.getName(), query);
	}

	@Override
	protected void onSelectionChanged(Project item) {
		ScanFilterCache.getInstance().refresh(false);

		projectView.setSelection(item);
	}

}
