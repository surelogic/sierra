package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public class ScanAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {
		boolean saveCancelled = true;
		// Bug 1075 Fix - Ask for saving editors
		if (!PreferenceConstants.alwaysSaveResources()) {
			saveCancelled = PlatformUI.getWorkbench().saveAllEditors(true);
		} else {
			PlatformUI.getWorkbench().saveAllEditors(false);
		}
		if (saveCancelled) {
			new Scan(selectedProjects).execute();
		}
	}
}
