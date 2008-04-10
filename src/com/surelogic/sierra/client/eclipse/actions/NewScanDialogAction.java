package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class NewScanDialogAction extends NewScanAction {
	@Override
	protected void run(final List<IJavaProject> selectedProjects,
			final List<String> projectNames) {
		final List<IJavaProject> projects = JavaProjectSelectionDialog
				.getProjects("Select project(s) to scan:", "Scan Project",
						SLImages.getImage(CommonImages.IMG_SIERRA_SCAN),
						selectedProjects);
		if (selectedProjects == projects) {
			/*
			 * We can use the passed set of project names.
			 */
			super.run(selectedProjects, projectNames);
		} else {
			super.run(projects, null);
		}
	}
}
