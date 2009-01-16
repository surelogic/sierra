package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class TroubleshootWrongAuthentication extends
		TroubleshootConnection {

	public TroubleshootWrongAuthentication(final ServerFailureReport method,
			ServerLocation location) {
		super(method, location);
	}

	@Override
	protected String getLabel() {
		return "Unable To Authenticate";
	}

	@Override
	protected IStatus createStatus() {
		return SLEclipseStatusUtility
				.createInfoStatus("Unable to authenticate to "
						+ getLocation().createHomeURL().toString());
	}

	private ServerLocation f_modifiedLocation = null;

	@Override
	protected ServerLocation showDialog() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				f_modifiedLocation = ServerAuthenticationDialog.open(null,
						getLocation());
			}
		});
		if (f_modifiedLocation == null)
			f_modifiedLocation = getLocation();
		final boolean modified = getLocation() != f_modifiedLocation;
		setRetry(!modified);
		return f_modifiedLocation;
	}
}
