package com.surelogic.sierra.client.eclipse.jobs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * Abstract base class for jobs that interact with a team server, and optionally
 * a project connected to that team server.
 */
public abstract class AbstractServerProjectJob extends AbstractServerJob {

	private ConnectedServer f_server;

	protected ConnectedServer getServer() {
		return f_server;
	}

	protected void changeAuthorization(final String user, final String pass,
			final boolean savePassword) {
		if (user == null)
			throw new IllegalArgumentException(I18N.err(44, "user"));
		if (pass == null)
			throw new IllegalArgumentException(I18N.err(44, "pass"));
		/*
		 * Tell the manager about this update
		 */
		f_server = ConnectedServerManager.getInstance().changeAuthorizationFor(
				f_server, user, pass, savePassword);
	}

	protected final String f_projectName;
	protected final ServerProjectGroupJob joinJob;
	protected final ServerFailureReport f_method;

	/**
	 * Construct a job.
	 * 
	 * @param family
	 *            the job family, may be {@code null}.
	 * @param name
	 *            the non-null name of the job.
	 * @param server
	 *            the non-null server to contact.
	 * @param project
	 *            the project name that this job is working with, may be {@code
	 *            null}.
	 * @param method
	 *            the method to report problems that the job encounters, may be
	 *            {@code null}.
	 */
	public AbstractServerProjectJob(final ServerProjectGroupJob family,
			final String name, final ConnectedServer server,
			final String project, final ServerFailureReport method) {
		super(name);
		joinJob = family;
		if (family != null) {
			joinJob.add(this);
		}
		f_server = server;
		f_projectName = project == null ? "(none)" : project;
		f_method = method;
	}

	protected final TroubleshootConnection getTroubleshootConnection(
			final ServerFailureReport method,
			final SierraServiceClientException e) {
		return getTroubleshootConnection(method, f_server.getLocation(), e);
	}

	protected final IStatus createWarningStatus(final int errNo,
			final Throwable t) {
		final String msg = I18N.err(errNo, f_projectName, f_server);
		final IStatus s = SLEclipseStatusUtility.createWarningStatus(errNo,
				msg, t);
		final String title = "Problem while " + getName();
		showErrorDialog(msg, t, title, s);
		return s;
	}

	protected final IStatus createErrorStatus(final int errNo, final Throwable t) {
		final String msg = I18N.err(errNo, f_projectName, f_server);
		final IStatus s = SLEclipseStatusUtility.createErrorStatus(errNo, msg,
				t);
		final String title = "Error while " + getName();
		showErrorDialog(msg, t, title, s);
		return s;
	}

	public void showErrorDialog(final String msg, final Throwable t,
			final String title, final IStatus s) {
		SLLogger.getLogger().log(Level.INFO, msg, t);
		final Job job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				// Note this will be automatically logged by Eclipse
				// when the original job returns
				ErrorDialogUtility.open(null, title, s, false);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
