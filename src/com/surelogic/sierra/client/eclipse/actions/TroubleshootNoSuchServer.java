package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootNoSuchServer extends TroubleshootConnection {

	public TroubleshootNoSuchServer(SierraServer server, String projectName) {
		super(server, projectName);
	}

	@Override
	public void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				final MessageBox dialog = new MessageBox(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_ERROR | SWT.APPLICATION_MODAL | SWT.OK);
				dialog.setText("Sierra Team Server Connection Failed");
				dialog
						.setMessage("The requested action failed because it was not possible to connect to the Sierra team server '"
								+ f_server.getLabel()
								+ "'.\n\n"
								+ "Possible reasons for this include:\n"
								+ " \u25CF The network is down or disconnected.\n"
								+ " \u25CF The Sierra team server '"
								+ f_server.getLabel()
								+ "' is turned off or is not responding.\n"
								+ " \u25CF The location settings for '"
								+ f_server.getLabel()
								+ "' are incorrect.\n\n"
								+ "Possible resolutions for this problem include:\n"
								+ " \u25CF Check your network connection to the Sierra team server '"
								+ f_server.getLabel()
								+ "'.\n"
								+ " \u25CF Check the Sierra team server '"
								+ f_server.getLabel()
								+ "'. is running.\n"
								+ " \u25CF Fix the location settings for '"
								+ getServer().getLabel()
								+ "' so that they are correct.");
				dialog.open();
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
