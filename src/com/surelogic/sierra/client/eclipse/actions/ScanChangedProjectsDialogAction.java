package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class ScanChangedProjectsDialogAction extends ScanChangedProjectsAction {
	@Override
	protected JavaProjectSelectionDialog.Configuration getDialogInfo(
			List<IJavaProject> selectedProjects) {
		return new JavaProjectSelectionDialog.Configuration(
				"Select project(s) to re-scan changes within:",
				"Re-Scan Changes in Project",
				SLImages.getImage(CommonImages.IMG_SIERRA_SCAN_DELTA),
				selectedProjects,
				SierraPreferencesUtility.ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN);
	}
}
