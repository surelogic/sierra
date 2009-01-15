package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public final class TroubleshootWrongServer extends TroubleshootConnection {

	public TroubleshootWrongServer(final ServerFailureReport method,
			                       ConnectedServer server, String projectName) {
		super(method, server, projectName);
	}
	@Override
	protected String getLabel() {
		return "Connected to the Wrong Sierra Team Server";
	}
	@Override
	protected IStatus createStatus() {
		final String sl = f_server.getName();
		final int errNo = 24;
		final String msg = I18N.err(errNo, sl, f_projectName, sl,
						getServer().getName(), f_projectName, sl, 
						getServer().getName());
		final IStatus reason = SLEclipseStatusUtility.createWarningStatus(errNo, msg);
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}
}
