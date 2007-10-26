package com.surelogic.sierra.client.eclipse.actions;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.ShareScanJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraConstants;

public final class PublishScanAction extends AbstractWebServiceMenuAction {
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
			final StringBuilder b = new StringBuilder();
			b.append("You must scan '");
			b.append(projectName);
			b.append("' before you can share the scan results to the server '");
			b.append(server.getLabel());
			b.append("'.\n\n");
			b.append("Possible reasons for this problem include:\n");
			b.append(" \u25CF You have never scanned the project '");
			b.append(projectName);
			b.append("'.\n");
			b.append(" \u25CF The file '");
			b.append(PreferenceConstants.getSierraPath());
			b.append(System.getProperty("file.separator"));
			b.append(projectName);
			b.append(".sierra.gz' has been deleted from the disk.\n\n");
			b.append("Possible resolutions for this problem include:\n");
			b.append(" \u25CF Run a scan on the project '");
			b.append(projectName);
			b.append("'.");

			MessageDialog.openError(shell, "No Scan Exists", b.toString());
		}
	}
}
