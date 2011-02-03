package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public class NewScanDialogAction extends NewScanAction {
	@Override
	protected JavaProjectSelectionDialog.Config getDialogInfo(List<IJavaProject> selectedProjects) {
		return new JavaProjectSelectionDialog.Config("Select project(s) to scan:", "Scan Project",
				SLImages.getImage(CommonImages.IMG_SIERRA_SCAN),
		        selectedProjects, PreferenceConstants.alwaysAllowUserToSelectProjectsToScan);
	}	
}
