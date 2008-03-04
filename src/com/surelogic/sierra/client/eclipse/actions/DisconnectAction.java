package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;

public class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {
		if (!projectNames.isEmpty())
			DeleteProjectDataJob.utility(projectNames, null, true);
	}
}
