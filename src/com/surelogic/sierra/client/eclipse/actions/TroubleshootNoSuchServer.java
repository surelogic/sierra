package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootNoSuchServer extends TroubleshootConnection {

	public TroubleshootNoSuchServer(SierraServer server) {
		super(server);
	}

	private int f_dialogResult;

	@Override
	void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ServerLocationDialog dialog = new ServerLocationDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), f_server,
						"Connection Failed: No Such Server");
				f_dialogResult = dialog.open();
			}
		});
		if (f_dialogResult == Window.CANCEL)
			setCanceled();
	}

}
