package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public class TroubleshootWrongServerVersion extends TroubleshootConnection {

	public TroubleshootWrongServerVersion(final ServerFailureReport strategy,
			final ServerLocation location) {
		super(strategy, location);
	}

	@Override
	protected IStatus createStatus() {
		final String sl = getLocation().createHomeURL().toString();
		final int errNo = 172;
		final String msg = I18N.err(errNo, sl);
		final IStatus reason = SLEclipseStatusUtility.createWarningStatus(
				errNo, msg);
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}

	@Override
	protected String getLabel() {
		return "The Sierra Team Server is of a different version than this client.";
	}

}
