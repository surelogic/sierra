package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.ServerInfoReply;
import com.surelogic.sierra.tool.message.ServerInfoRequest;
import com.surelogic.sierra.tool.message.ServerInfoService;
import com.surelogic.sierra.tool.message.ServerInfoServiceClient;
import com.surelogic.sierra.tool.message.ServerLocation;

/**
 * Updates information about a team server and, typically, (but not always)
 * validates that the client can talk to a particular team server and
 * synchronizes data.
 */
public class ValidateServerLocationJob extends AbstractServerProjectJob {

	/**
	 * Constructs a job.
	 * 
	 * @param server
	 *            the non-null server to contact.
	 */
	public ValidateServerLocationJob(ServerLocation location,
			boolean savePassword, boolean autoSync) {
		super(null, "Validating conection to " + location.getHost(),
				null /* TODO */, null, ServerFailureReport.SHOW_DIALOG);
	}

	// static void updateServer(final ServerLocationDialog dialog,
	// final SierraServer server) {
	// if (dialog != null) {
	// boolean changed = server.setServer(dialog.f_server,
	// dialog.f_serverReply);
	// changed = changed
	// || (server.savePassword() != dialog.f_savePassword);
	//
	// server.setSavePassword(dialog.f_savePassword);
	//
	// changed = changed || (server.autoSync() != dialog.f_autoSync);
	// server.setAutoSync(dialog.f_autoSync);
	//
	// if (changed) {
	// /*
	// * Because we probably changed something about the server,
	// * notify all observers of server information.
	// */
	// server.getManager().notifyObservers();
	// }
	// /*
	// * If we were able to validate this server connection then we do an
	// * automatic BugLink synchronize with it.
	// */
	// if (dialog.f_connectionToServerHasBeenValidated) {
	// final SynchronizeBugLinkServerAction sync = new
	// SynchronizeBugLinkServerAction(
	// ServerFailureReport.SHOW_DIALOG, true);
	// sync.run(server);
	// }
	// }
	// }

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Trying to connect...", IProgressMonitor.UNKNOWN);
		try {
			/*
			 * First we validate our connection to the team server.
			 */
			final ServerLocation server = getServer().getLocation();

			boolean success = false; // assume the worst
			final ServerInfoService ss = ServerInfoServiceClient.create(server);
			try {
				final ServerInfoReply serverReply = ss
						.getServerInfo(new ServerInfoRequest());
				final String uid = serverReply.getUid();
				if (uid != null) {
					success = true;
				}
			} catch (final Exception e) {
				// nothing to do, we could not connect
			}

			if (success) {
				/*
				 * We can talk to this team server. Next, we synchronize BugLink
				 * information with it.
				 */
				final SynchronizeJob job = new SynchronizeJob(null, null,
						getServer(), ServerSyncType.BUGLINK, true, f_method);
				job.schedule();
			} else {
				/*
				 * We can't talk to the team server, better let the user know so
				 * that they can take action.
				 */

			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}
