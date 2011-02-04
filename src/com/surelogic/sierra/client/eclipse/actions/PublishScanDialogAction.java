package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class PublishScanDialogAction extends PublishScanAction {
	@Override
	protected JavaProjectSelectionDialog.Config getDialogInfo(
			List<IJavaProject> selectedProjects) {
		return new JavaProjectSelectionDialog.Config(
				"Select project(s) to publish:",
				"Publish Scan for Project",
				SLImages.getImage(CommonImages.IMG_SIERRA_PUBLISH),
				selectedProjects,
				SierraPreferencesUtility.ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN);
	}
}
