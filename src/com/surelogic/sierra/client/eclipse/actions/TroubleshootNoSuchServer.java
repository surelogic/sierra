package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootNoSuchServer extends TroubleshootConnection {

	public TroubleshootNoSuchServer(SierraServer server, String projectName) {
		super(server, projectName);
	}

	@Override
	public void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				final StringBuilder b = new StringBuilder();
				b.append("The requested action failed because it was ");
				b.append("not possible to connect to the Sierra team server '");
				b.append(f_server.getLabel());
				b.append("'.\n\n");
				b.append("Possible reasons for this problem include:\n");
				b.append(" \u25CF The network is down or disconnected.\n");
				b.append(" \u25CF The Sierra team server '");
				b.append(f_server.getLabel());
				b.append("' is turned off or is not responding.\n");
				b.append(" \u25CF The location settings for '");
				b.append(f_server.getLabel());
				b.append("' are incorrect.\n\n");
				b.append("Possible resolutions for this problem include:\n");
				b.append(" \u25CF Check your network connection");
				b.append(" to the Sierra team server '");
				b.append(f_server.getLabel());
				b.append("'.\n");
				b.append(" \u25CF Check the Sierra team server '");
				b.append(f_server.getLabel());
				b.append("' is running.\n");
				b.append(" \u25CF Fix the location settings for '");
				b.append(getServer().getLabel());
				b.append("' so that they are correct.");
				final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(),
						"Sierra Team Server Connection Failed", null, b
								.toString(), null, Activator.getDefault());
				report.open();
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
