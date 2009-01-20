package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.InvalidServerException;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * Updates information about a team server and, typically, (but not always)
 * validates that the client can talk to a particular team server and
 * synchronizes data.
 */
public class ValidateServerLocationJob extends DatabaseJob {

	private final ServerLocation f_loc;
	private final boolean f_savePass;

	/**
	 * Constructs a job.
	 * 
	 * @param server
	 *            the non-null server to contact.
	 */
	public ValidateServerLocationJob(final ServerLocation location,
			final boolean savePassword, final boolean autoSync) {
		super("Validating connection to " + location.getHost());
		f_loc = location;
		f_savePass = savePassword;
	}

	private ConnectedServer validate(final ServerLocation loc,
			final boolean savePassword) {
		try {
			return Data.getInstance().withTransaction(
					SettingQueries
							.checkAndSaveServerLocation(loc, savePassword));
		} catch (final SierraServiceClientException e) {
			TroubleshootConnection c;
			if (e instanceof InvalidLoginException) {
				c = new TroubleshootWrongAuthentication(
						ServerFailureReport.SHOW_DIALOG, loc);
			} else {
				c = new TroubleshootNoSuchServer(
						ServerFailureReport.SHOW_DIALOG, loc);
			}
			final ServerLocation updLoc = c.fix();
			if (c.retry()) {
				return validate(updLoc, savePassword);
			} else {
				throw e;
			}
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask("Trying to connect...", IProgressMonitor.UNKNOWN);
		try {
			final ConnectedServer server = validate(f_loc, f_savePass);
			/*
			 * We can talk to this team server. Next, we synchronize BugLink
			 * information with it.
			 */
			final SynchronizeJob job = new SynchronizeJob(null, null, server,
					ServerSyncType.BUGLINK, true,
					ServerFailureReport.SHOW_DIALOG);
			job.schedule();
			DatabaseHub.getInstance().notifyServerSynchronized();
			return Status.OK_STATUS;
		} catch (final Exception e) {
			if (e instanceof TransactionException
					&& e.getCause() instanceof InvalidServerException) {
				return SLEclipseStatusUtility.createErrorStatus(158, e
						.getCause().getMessage());
			} else {
				return SLEclipseStatusUtility.createErrorStatus(159, e);
			}
		} finally {
			monitor.done();
		}
	}
}
