package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootWrongAuthentication extends
		TroubleshootConnection {

	public TroubleshootWrongAuthentication(SierraServer server, String projectName) {
		super(server, projectName);
	}

	private int f_dialogResult;

	@Override
	public void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ServerAuthenticationDialog dialog = new ServerAuthenticationDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), f_server);
				f_dialogResult = dialog.open();
			}
		});
		setRetry(f_dialogResult != Window.CANCEL);
	}
}
