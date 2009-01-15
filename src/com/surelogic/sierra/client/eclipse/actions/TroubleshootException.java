package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public final class TroubleshootException extends TroubleshootConnection {
	private final boolean f_isSevere;
	private Exception f_ex;

	public TroubleshootException(final ServerFailureReport method,
			                     ConnectedServer server, String projectName, 
								 Exception e, boolean severe) {
		super(method, server, projectName);
		f_ex = e;
		f_isSevere = severe;
	}

	@Override
	protected String getLabel() {
		return "Problem Getting Server Info";
	}
	@Override
	protected IStatus createStatus() {
		final String sl = f_server.getName();
		final int errNo = 97;
		final String msg = I18N.err(errNo, f_projectName, sl, 
				                    f_ex.getClass().getName(),
				                    f_ex.getMessage());
		final IStatus reason;
		if (f_isSevere) {
			reason = SLEclipseStatusUtility.createErrorStatus(errNo, msg, f_ex);
		} else {
			reason = SLEclipseStatusUtility.createWarningStatus(errNo, msg, f_ex);		
		}			
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}
	
	@Override
	public boolean failServer() {
		return false;
	}
}
