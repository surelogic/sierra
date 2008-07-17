package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import com.surelogic.common.eclipse.logging.SLStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public final class TroubleshootException extends TroubleshootConnection {
	private final boolean f_isSevere;
	private Exception f_ex;

	public TroubleshootException(final ServerFailureReport method,
			                     SierraServer server, String projectName, 
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
		final String sl = f_server.getLabel();
		final int errNo = 97;
		final String msg = I18N.err(errNo, f_projectName, sl, 
				                    f_ex.getClass().getName(),
				                    f_ex.getMessage());
		final IStatus reason;
		if (f_isSevere) {
			reason = SLStatusUtility.createErrorStatus(errNo, msg, f_ex);
		} else {
			reason = SLStatusUtility.createWarningStatus(errNo, msg, f_ex);		
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
