package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

/**
 * @author Edwin.Chan
 */
public class NewScanAction extends AbstractProjectSelectedMenuAction {
	@Override
	protected void run(final List<IJavaProject> selectedProjects,
			final List<String> projectNames) {
		NewScan s = new NewScan();
		if (projectNames == null || projectNames.isEmpty()) {
			s.scan(selectedProjects);
		} else {
			s.scan(selectedProjects, projectNames);
		}
	}
}
