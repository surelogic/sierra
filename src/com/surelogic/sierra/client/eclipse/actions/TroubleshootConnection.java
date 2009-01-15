package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public abstract class TroubleshootConnection {

	protected final ServerFailureReport f_method;
	protected final ConnectedServer f_server;
	protected final String f_projectName;
	protected IStatus f_status;

	/**
	 * Constructs this object.
	 * 
	 * @param server
	 *            the mutable server configuration to be fixed.
	 * @param projectName
	 *            the project name, or <code>null</code> if no project or it
	 *            is unknown.
	 */
	protected TroubleshootConnection(final ServerFailureReport method,
			final ConnectedServer server, final String projectName) {
		f_method = method;

		if (server == null)
			throw new IllegalStateException("server must be non-null");
		f_server = server;
		if (projectName != null)
			f_projectName = projectName;
		else
			f_projectName = "(unknown)";
	}

	public final ConnectedServer getServer() {
		return f_server;
	}

	public final String getProjectName() {
		return f_projectName;
	}

	private boolean f_retry = true;

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
	 * Tries to fix the server location and authentication data passed in to the
	 * constructor of this object.
	 * <p>
	 * Subclasses must override to take the appropriate UI actions to mutate the
	 * server object.
	 */
	public final void fix() {
		f_status = createStatus();
		switch (f_method) {
		default:
		case SHOW_BALLOON:
			showBalloon();
			setRetry(false);
			break;
		case SHOW_DIALOG:
			showDialog();
			break;
		case IGNORE:
			SLLogger.getLogger().log(Level.WARNING, f_status.getMessage(),
					f_status.getException());
			setRetry(false);
		}

		if (failServer()) {
			ConnectedServerManager.getInstance().getStats(f_server).encounteredProblem();
			ConnectedServerManager.getInstance().notifyObservers();
		} else {
			// Notifies observers by itself
			Projects.getInstance().encounteredProblem(f_projectName);
		}
	}

	protected void showDialog() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ErrorDialogUtility.open(null, getLabel(), f_status);
			}
		});
	}

	private void showBalloon() {
		SLLogger.getLogger().log(Level.WARNING, f_status.getMessage(),
				f_status.getException());
		BalloonUtility.showMessage(getLabel(), f_status.getMessage());
	}

	protected abstract IStatus createStatus();

	protected abstract String getLabel();

	/**
	 * @return true if the server should be considered failed
	 */
	public boolean failServer() {
		return !f_retry;
	}
}
