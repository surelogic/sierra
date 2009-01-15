package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public final class TroubleshootWrongAuthentication extends
		TroubleshootConnection {

	public TroubleshootWrongAuthentication(final ServerFailureReport method,
			ConnectedServer server, String projectName) {
		super(method, server, projectName);
	}

	private int f_dialogResult;

	@Override
	protected String getLabel() {
		return "Unable To Authenticate";
	}

	@Override
	protected IStatus createStatus() {
		return SLEclipseStatusUtility
				.createInfoStatus("Unable to authenticate to "
						+ f_server.getName());
	}

	@Override
	protected void showDialog() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ServerAuthenticationDialog dialog = new ServerAuthenticationDialog(
						SWTUtility.getShell(), f_server);
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
