package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootWrongServer extends TroubleshootConnection {

	public TroubleshootWrongServer(SierraServer server, String projectName) {
		super(server, projectName);
	}

	@Override
	public void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				final MessageBox dialog = new MessageBox(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.ICON_ERROR | SWT.APPLICATION_MODAL | SWT.OK);
				dialog.setText("Connected to the Wrong Sierra Team Server");
				dialog
						.setMessage("The requested action failed because the Sierra team server '"
								+ f_server.getLabel()
								+ "' is not the Sierra team server that the project '"
								+ f_projectName
								+ "' has previously connected to and exchanged data with.\n\n"
								+ "Possible reasons for this include:\n"
								+ " \u25CF The Sierra team server '"
								+ f_server.getLabel()
								+ "' has been reinstalled (or its database has been erased or mutated).\n"
								+ " \u25CF The location settings for '"
								+ getServer().getLabel()
								+ "' have been changed or are incorrect.\n\n"
								+ "Possible resolutions for this problem include:\n"
								+ " \u25CF Disconnect the project '"
								+ f_projectName
								+ "' from the Sierra team server '"
								+ f_server.getLabel()
								+ "'.  The project can then be connected to any Sierra team server.\n"
								+ " \u25CF Fix the location settings for '"
								+ getServer().getLabel()
								+ "' so that they point the the Sierra team server that the project has previously exchanged data with.");
				dialog.open();
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
