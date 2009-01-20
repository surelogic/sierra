package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseAccessRule;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongServer;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * This job explicitly adds the {@link DatabaseAccessRule} to run like a
 * database job.
 */
public class SynchronizeJob extends AbstractServerProjectJob {
	private final boolean f_force;
	private final ServerSyncType f_syncType;

	/**
	 * Constructs a job.
	 * 
	 * @param family
	 *            the job family, may be {@code null}.
	 * @param projectName
	 *            the name of a project being synchronized, may be {@code null}
	 *            if just a BugLink synchronize with the server.
	 * @param server
	 *            the non-null server to contact.
	 * @param syncType
	 *            the non-null {@link ServerSyncType}.
	 * @param force
	 *            when {@code true} it causes the synchronize to be attempted
	 *            even if too many failures have occurred in the past.
	 * @param strategy
	 *            the method to report problems that the job encounters.
	 */
	public SynchronizeJob(final ServerProjectGroupJob family,
			final String projectName, final ConnectedServer server,
			final ServerSyncType syncType, final boolean force,
			final ServerFailureReport strategy) {
		super(
				family,
				syncType.equals(ServerSyncType.BUGLINK) ? "Synchronizing BugLink data for server '"
						+ server.getName() + "'"
						: "Synchronizing Sierra data for project '"
								+ projectName + "'", server, projectName,
				strategy);
		f_force = force;
		f_syncType = syncType;
		setRule(DatabaseAccessRule.getInstance());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final int threshold = PreferenceConstants
				.getServerInteractionRetryThreshold();
		final int numProblems = ConnectedServerManager.getInstance().getStats(
				getServer()).getProblemCount()
				+ Projects.getInstance().getProblemCount(f_projectName);
		if (!f_force && (numProblems > threshold)) {
			return Status.CANCEL_STATUS;
		}
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, getName());
		slMonitor.begin(6);
		try {
			return synchronize(new SyncTransaction(slMonitor), slMonitor);
		} catch (final Throwable e) {
			return createWarningStatus(51, e);
		}
	}

	private IStatus synchronize(final DBTransaction<IStatus> sync,
			final SLProgressMonitor slMonitor) {
		try {
			return Data.getInstance().withTransaction(sync);
		} catch (final TransactionException e) {
			final Throwable cause = e.getCause();
			TroubleshootConnection troubleshoot = null;
			if (joinJob != null && joinJob.troubleshoot(getServer())) {
				if (cause instanceof ServerMismatchException) {
					troubleshoot = new TroubleshootWrongServer(f_strategy,
							getServer().getLocation(), f_projectName);
				} else if (cause instanceof SierraServiceClientException) {
					troubleshoot = getTroubleshootConnection(f_strategy,
							(SierraServiceClientException) cause);
				}
				if (troubleshoot != null && troubleshoot.retry()) {
					final ServerLocation fixed = troubleshoot.fix();
					if (troubleshoot.retry()) {
						/*
						 * First update our server information.
						 */
						changeAuthorization(fixed.getUser(), fixed.getPass(),
								fixed.isSavePassword());
						return synchronize(sync, slMonitor);
					}
				}
			}
			joinJob.fail(getServer());
			return fail(e);
		}
	}

	private class SyncTransaction implements DBTransaction<IStatus> {
		private final SLProgressMonitor slMonitor;

		SyncTransaction(final SLProgressMonitor slMonitor) {
			this.slMonitor = slMonitor;
		}

		public IStatus perform(final Connection conn) throws Exception {
			final Query q = new ConnectionQuery(conn);
			SettingQueries.updateServerInfo(getServer().getLocation()).perform(
					q);
			if (f_syncType.syncBugLink()
					&& (joinJob == null || joinJob.process(getServer()))) {
				final ServerLocation loc = getServer().getLocation();
				SettingQueries.retrieveCategories(loc,
						SettingQueries.categoryRequest().perform(q)).perform(q);
				SettingQueries.retrieveScanFilters(loc,
						SettingQueries.scanFilterRequest().perform(q)).perform(
						q);
				slMonitor.worked(1);
			}
			if (f_syncType.syncProjects()) {
				ClientProjectManager.getInstance(conn).synchronizeProject(
						getServer(), f_projectName, slMonitor);
			}
			ConnectedServerManager.getInstance().getStats(getServer())
					.markAsConnected();
			if (slMonitor.isCanceled()) {
				conn.rollback();
				return Status.CANCEL_STATUS;
			} else {
				DatabaseHub.getInstance().notifyServerSynchronized();
				return Status.OK_STATUS;
			}
		}
	}

	private IStatus fail(final Exception e) {
		final String msg = I18N.err(51, f_projectName, getServer());
		SLLogger.getLogger().log(Level.WARNING, msg, e);
		return Status.CANCEL_STATUS;
	}
}
