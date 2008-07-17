package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.logging.SLStatusUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public final class TroubleshootWrongAuthentication extends
		TroubleshootConnection {

	public TroubleshootWrongAuthentication(final ServerFailureReport method,
			                               SierraServer server,
			String projectName) {
		super(method, server, projectName);
	}

	private int f_dialogResult;

	@Override
	protected String getLabel() {
		return "Unable To Authenicate";
	}
	@Override
	protected IStatus createStatus() {
		return SLStatusUtility.createInfoStatus("Unable to authenicate to "+
				                         f_server.getLabel());
	}
	
	@Override
	protected void showDialog() {
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
	
	@Override
	public boolean failServer() {
		return false;
	}
}
