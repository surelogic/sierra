package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.dialogs.MessageDialog;
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
				final StringBuilder b = new StringBuilder();
				b.append("The requested action failed because");
				b.append(" the Sierra team server '");
				b.append(f_server.getLabel());
				b.append("' is not the Sierra team server that the project '");
				b.append(f_projectName);
				b.append("' has previously connected to");
				b.append(" and exchanged data with.\n\n");
				b.append("Possible reasons for this problem include:\n");
				b.append(" \u25CF The Sierra team server '");
				b.append(f_server.getLabel());
				b.append("' has been reinstalled (or its database");
				b.append(" has been erased or mutated).\n");
				b.append(" \u25CF The location settings for '");
				b.append(getServer().getLabel());
				b.append("' have been changed or are incorrect.\n\n");
				b.append("Possible resolutions for this problem include:\n");
				b.append(" \u25CF Disconnect the project '");
				b.append(f_projectName);
				b.append("' from the Sierra team server '");
				b.append(f_server.getLabel());
				b.append("'.  The project can then be connected");
				b.append(" to any Sierra team server.\n");
				b.append(" \u25CF Fix the location settings for '");
				b.append(getServer().getLabel());
				b.append("' so that they point the the Sierra team server");
				b.append(" that the project has previously");
				b.append(" exchanged data with.");
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"Connected to the Wrong Sierra Team Server", b
								.toString());
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
