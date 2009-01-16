package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class TroubleshootException extends TroubleshootConnection {

	private final boolean f_isSevere;
	private Exception f_e;

	public TroubleshootException(final ServerFailureReport method,
			ServerLocation location, Exception e, boolean severe) {
		super(method, location);
		f_e = e;
		f_isSevere = severe;
	}

	@Override
	protected String getLabel() {
		return "Problem Getting Server Information";
	}

	@Override
	protected IStatus createStatus() {
		final String sl = getLocation().createHomeURL().toString();
		final int errNo = 97;
		final String msg = I18N.err(errNo, sl, f_e.getClass().getName(), f_e
				.getMessage());
		final IStatus reason;
		if (f_isSevere) {
			reason = SLEclipseStatusUtility.createErrorStatus(errNo, msg, f_e);
		} else {
			reason = SLEclipseStatusUtility
					.createWarningStatus(errNo, msg, f_e);
		}
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}
}
