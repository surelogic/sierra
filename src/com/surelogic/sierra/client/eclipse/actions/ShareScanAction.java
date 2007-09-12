package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.ShareScanJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraConstants;

public final class ShareScanAction extends AbstractWebServiceMenuAction {
	@Override
	void run(String projectName, SierraServer server, Shell shell) {
		final String scanFileName = PreferenceConstants.getSierraPath()
				+ File.separator + projectName
				+ SierraConstants.PARSED_FILE_SUFFIX;
		final File scanFile = new File(scanFileName);
		if (scanFile.exists()) {
			ShareScanJob job = new ShareScanJob(projectName, server, scanFile);
			job.schedule();
		} else {
			final MessageBox message = new MessageBox(shell, SWT.ICON_ERROR
					| SWT.APPLICATION_MODAL | SWT.OK);
			message.setText("No Scan Exists");
			message.setMessage("You must scan '" + projectName
					+ "' before you can share the scan results to the server '"
					+ server.getLabel() + "'.");
			message.open();
		}
	}
}
