package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.common.jobs.NullSLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.license.SLLicenseUtility;
import com.surelogic.common.logging.SLLogger;

/**
 * @author Edwin.Chan
 */
public class NewScanAction extends AbstractProjectSelectedMenuAction {
	@Override
	protected void run(final List<IJavaProject> selectedProjects,
			final List<String> projectNames) {

		/*
		 * License check: A hack because Sierra is not using SLJobs yet.
		 */
		final SLStatus failed = SLLicenseUtility.validateSLJob(
				SLLicenseUtility.SIERRA_SUBJECT, new NullSLProgressMonitor());
		if (failed != null) {
			SLLogger.getLogger().log(failed.getSeverity().toLevel(),
					failed.getMessage(), failed.getException());
			return;
		}

		NewScan s = new NewScan();
		if (projectNames == null || projectNames.isEmpty()) {
			s.scan(selectedProjects);
		} else {
			s.scan(selectedProjects, projectNames);
		}
	}
}
