package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.ui.dialogs.ErrorDialogUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.ShareScanJob;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class PublishScanAction extends AbstractWebServiceMenuAction {
	@Override
	void runServerAction(final ServerProjectGroupJob family,
			String projectName, ConnectedServer server, Shell shell) {
		final File scanFile = NewScan.findScanDocumentFile(projectName);
		if (scanFile != null) {
			final ShareScanJob job = new ShareScanJob(family, projectName,
					server, scanFile, ServerFailureReport.SHOW_DIALOG);
			job.schedule();
		} else {
			final int errNo = 21;
			final String msg = I18N.err(errNo, projectName, server.getName(),
					projectName, PreferenceConstants.getSierraDataDirectory()
							.getAbsolutePath(), File.separator, projectName,
					projectName);
			final IStatus reason = SLEclipseStatusUtility.createErrorStatus(
					errNo, msg);
			ErrorDialogUtility.open(shell, "No Scan Exists", reason);
		}
	}
}
