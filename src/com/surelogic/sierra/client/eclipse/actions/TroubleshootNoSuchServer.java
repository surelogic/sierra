package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class TroubleshootNoSuchServer extends TroubleshootConnection {

	public TroubleshootNoSuchServer(final ServerFailureReport strategy,
			ServerLocation location) {
		super(strategy, location);
	}

	@Override
	protected String getLabel() {
		return "Sierra Team Server Connection Failed";
	}

	@Override
	protected IStatus createStatus() {
		final String sl = getLocation().createHomeURL().toString();
		final int errNo = 23;
		final String msg = I18N.err(errNo, sl, sl, sl, sl, sl, sl);
		final IStatus reason = SLEclipseStatusUtility.createWarningStatus(
				errNo, msg);
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}
}
