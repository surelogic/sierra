package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class SynchronizeProjectDialogAction extends SynchronizeProjectAction {
	@Override
	protected JavaProjectSelectionDialog.Configuration getDialogInfo(
			List<IJavaProject> selectedProjects) {
		return new JavaProjectSelectionDialog.Configuration(
				"Select project(s) to synchronize:",
				"Synchronize Project",
				SLImages.getImage(CommonImages.IMG_SIERRA_SYNC),
				selectedProjects,
				SierraPreferencesUtility.ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN,
				null);
	}
}
