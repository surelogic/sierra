package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.actions.AbstractProjectSelectedMenuAction;
import com.surelogic.common.ui.dialogs.JavaProjectSelectionDialog;
import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.SLLicenseProduct;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;

public class NewScanAction extends AbstractProjectSelectedMenuAction {
	@Override
	protected void runActionOn(final List<IJavaProject> selectedProjects) {
		if (selectedProjects == null || selectedProjects.isEmpty())
			return;

		/*
		 * License check: A hack because Sierra is not using SLJobs yet.
		 */
		final SLStatus failed = SLLicenseUtility.validateSLJob(
				SLLicenseProduct.SIERRA, new NullSLProgressMonitor());
		if (failed != null) {
			SLLogger.getLogger().log(failed.getSeverity().toLevel(),
					failed.getMessage(), failed.getException());
			return;
		}
		(new NewScan()).scan(selectedProjects);
	}

	@Override
	protected JavaProjectSelectionDialog.Configuration getDialogInfo(
			List<IJavaProject> selectedProjects) {
		return new JavaProjectSelectionDialog.Configuration(
				"Select project(s) to scan:",
				"Scan Project",
				SLImages.getImage(CommonImages.IMG_SIERRA_SCAN),
				selectedProjects,
				SierraPreferencesUtility.ALWAYS_ALLOW_USER_TO_SELECT_PROJECTS_TO_SCAN,
				SierraPreferencesUtility.LAST_TIME_PROJECTS_TO_SCAN);
	}
}
