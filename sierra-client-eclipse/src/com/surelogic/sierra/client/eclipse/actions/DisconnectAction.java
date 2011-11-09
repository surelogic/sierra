package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.ui.actions.AbstractProjectSelectedMenuAction;
import com.surelogic.sierra.client.eclipse.jobs.DeleteProjectDataJob;

public class DisconnectAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void runActionOn(List<IJavaProject> selectedProjects) {
		final List<String> projectNames = getNames(selectedProjects);
		if (!projectNames.isEmpty())
			DeleteProjectDataJob.utility(projectNames, null, true);
	}
}
