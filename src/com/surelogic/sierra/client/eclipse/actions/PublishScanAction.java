package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.FileUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.ShareScanJob;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.SierraToolConstants;

public class PublishScanAction extends AbstractWebServiceMenuAction {
	@Override
	void runServerAction(final ServerProjectGroupJob family,
			String projectName, ConnectedServer server, Shell shell) {
		final String scanFileName = projectName
				+ SierraToolConstants.PARSED_FILE_SUFFIX;
		final File scanFile = new File(FileUtility.getSierraDataDirectory(),
				scanFileName);
		if (scanFile.exists()) {
			final ShareScanJob job = new ShareScanJob(family, projectName,
					server, scanFile, ServerFailureReport.SHOW_DIALOG);
			job.schedule();
		} else {
			final int errNo = 21;
			final String msg = I18N.err(errNo, projectName, server.getName(),
					projectName, FileUtility.getSierraDataDirectory()
							.getAbsolutePath(), File.separator, projectName,
					projectName);
			final IStatus reason = SLEclipseStatusUtility.createErrorStatus(
					errNo, msg);
			ErrorDialogUtility.open(shell, "No Scan Exists", reason);
		}
	}
}
