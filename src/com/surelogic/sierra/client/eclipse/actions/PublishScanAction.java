package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.jobs.ShareScanJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraToolConstants;

public final class PublishScanAction extends AbstractWebServiceMenuAction {
	@Override
	void runServerAction(String projectName, SierraServer server, Shell shell) {
		final String scanFileName = PreferenceConstants.getSierraPath()
				+ File.separator + projectName
				+ SierraToolConstants.PARSED_FILE_SUFFIX;
		final File scanFile = new File(scanFileName);
		if (scanFile.exists()) {
			final ShareScanJob job = new ShareScanJob(projectName, server,
					scanFile);
			job.schedule();
		} else {
			final String msg = I18N.err(21, projectName, server.getLabel(),
					projectName, PreferenceConstants.getSierraPath(), System
							.getProperty("file.separator"), projectName,
					projectName);
			final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
					shell, "No Scan Exists", null, msg, null, Activator
							.getDefault());
			report.open();
			SLLogger.getLogger().warning(msg);
		}
	}
}
