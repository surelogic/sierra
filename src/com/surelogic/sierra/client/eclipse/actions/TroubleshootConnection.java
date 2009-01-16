package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerLocation;

public abstract class TroubleshootConnection {

	private final ServerFailureReport f_strategy;
	private final ServerLocation f_location;
	private IStatus f_status;
	private boolean f_retry = true;

	/**
	 * Constructs a instance to troubleshoot the passed server location via the
	 * passed method.
	 * 
	 * @param strategy
	 *            the strategy used to troubleshoot the passed server location.
	 * @param location
	 *            the server location to troubleshoot.
	 */
	protected TroubleshootConnection(final ServerFailureReport strategy,
			final ServerLocation location) {
		f_strategy = strategy == null ? ServerFailureReport.SHOW_DIALOG
				: strategy;
		if (location == null)
			throw new IllegalStateException("server must be non-null");
		f_location = location;
	}

	public final ServerLocation getLocation() {
		return f_location;
	}

	protected void setRetry(boolean retry) {
		f_retry = retry;
	}

	/**
	 * Indicates if the troubleshooting object wants the job to retry the action
	 * that was troubleshooted.
	 * 
	 * @return <code>true</code> if a retry should be attempted,
	 *         <code>false</code> otherwise.
	 */
	public final boolean retry() {
		return f_retry;
	}

	/**
	 * Indicates if the server location should be considered bad. This is the
	 * same as invoking {@code !}{@link #retry()}.
	 * 
	 * @return {@code true} if the server location should be considered bad,
	 *         {@code false} otherwise.
	 */
	public final boolean isServerConsideredBad() {
		return !f_retry;
	}

	/**
	 * Tries to troubleshoot the passed server location and return a new (and
	 * hopefully fixed) server location object.
	 * 
	 * @return a new server location object that reflects the changes made by
	 *         the user. If the returned object is the same object that was
	 *         passed via <tt>location</tt> then nothing was done.
	 */
	public final ServerLocation fix() {
		ServerLocation result = f_location;
		f_status = createStatus();
		switch (f_strategy) {
		default:
		case SHOW_BALLOON:
			showBalloon();
			setRetry(false);
			break;
		case SHOW_DIALOG:
			result = showDialog();
			break;
		case IGNORE:
			SLLogger.getLogger().log(Level.WARNING, f_status.getMessage(),
					f_status.getException());
			setRetry(false);
		}
		return result;
	}

	protected ServerLocation showDialog() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				ErrorDialogUtility.open(null, getLabel(), f_status);
			}
		});
		return f_location;
	}

	private void showBalloon() {
		SLLogger.getLogger().log(Level.WARNING, f_status.getMessage(),
				f_status.getException());
		BalloonUtility.showMessage(getLabel(), f_status.getMessage());
	}

	/**
	 * Gets a status for error reporting.
	 * 
	 * @return a status for error reporting.
	 */
	protected abstract IStatus createStatus();

	/**
	 * Gets a label, or short description, for error reporting.
	 * 
	 * @return a label, or short description, for error reporting.
	 */
	protected abstract String getLabel();
}
