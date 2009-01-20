package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;

import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public final class TroubleshootWrongServer extends TroubleshootConnection {

	private final String f_projectName;

	public TroubleshootWrongServer(final ServerFailureReport strategy,
			ServerLocation location, String projectName) {
		super(strategy, location);
		f_projectName = projectName;
	}

	@Override
	protected String getLabel() {
		return "Connected to the Wrong Sierra Team Server";
	}

	@Override
	protected IStatus createStatus() {
		final String sl = getLocation().createHomeURL().toString();
		final int errNo = 24;
		final String msg = I18N.err(errNo, sl, f_projectName, sl, sl,
				f_projectName, sl, sl);
		final IStatus reason = SLEclipseStatusUtility.createWarningStatus(
				errNo, msg);
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
		return reason;
	}
}
