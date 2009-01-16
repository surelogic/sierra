package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * Updates information about a team server and, typically, (but not always)
 * validates that the client can talk to a particular team server and
 * synchronizes data.
 */
public class ValidateServerLocationJob extends AbstractServerProjectJob {

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
		super(null, "Validating conection to " + location.getHost(),
				null /* TODO */, null, ServerFailureReport.SHOW_DIALOG);
		f_loc = location;
		f_savePass = savePassword;
	}

	private IStatus validate(final ServerLocation loc,
			final boolean savePassword) {
		try {
			f_server = Data.getInstance().withTransaction(
					SettingQueries
							.checkAndSaveServerLocation(loc, savePassword));
			return Status.OK_STATUS;
		} catch (final SierraServiceClientException e) {
			final TroubleshootConnection c = getTroubleshootConnection(
					f_method, f_loc, e);
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
			final IStatus status = validate(f_loc, f_savePass);
			if (status == Status.OK_STATUS) {
				/*
				 * We can talk to this team server. Next, we synchronize BugLink
				 * information with it.
				 */
				final SynchronizeJob job = new SynchronizeJob(null, null,
						getServer(), ServerSyncType.BUGLINK, true, f_method);
				job.schedule();
			}
			return status;
		} catch (final Exception e) {
			return createErrorStatus(51, e);
		} finally {
			monitor.done();
		}
	}
}
