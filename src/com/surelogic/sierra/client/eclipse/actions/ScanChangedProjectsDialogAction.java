package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.dialogs.JavaProjectSelectionDialog;

public class ScanChangedProjectsDialogAction extends ScanChangedProjectsAction {
	@Override
	public void run(List<IJavaProject> projects) {
		projects = JavaProjectSelectionDialog.getProjects(
				"Select project(s) to re-scan changes within:",
				"Re-Scan Changes in Project", SLImages
						.getImage(CommonImages.IMG_SIERRA_SCAN_DELTA), projects);
		super.run(projects);
	}
}
