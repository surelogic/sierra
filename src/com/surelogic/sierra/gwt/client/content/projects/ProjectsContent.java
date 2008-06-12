package com.surelogic.sierra.gwt.client.content.projects;

import com.google.gwt.user.client.ui.DockPanel;
import com.surelogic.sierra.gwt.client.ContentComposite;
import com.surelogic.sierra.gwt.client.Context;

public class ProjectsContent extends ContentComposite {
	private static final ProjectsContent instance = new ProjectsContent();

	public static ProjectsContent getInstance() {
		return instance;
	}

	private ProjectsContent() {
		super();
		// singleton
	}

	@Override
	protected void onInitialize(DockPanel rootPanel) {
		setCaption("Projects");

		// TODO Auto-generated method stub

	}

	@Override
	protected void onUpdate(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDeactivate() {
		// TODO Auto-generated method stub

	}

}
