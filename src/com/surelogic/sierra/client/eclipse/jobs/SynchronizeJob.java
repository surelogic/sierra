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
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public class SynchronizeJob extends AbstractServerProjectJob {
	private final boolean force;
	private final ServerSyncType syncType;

	public SynchronizeJob(final ServerProjectGroupJob family,
			final String projectName, final SierraServer server,
			final ServerSyncType sync, final boolean force,
			final ServerFailureReport method) {
		super(
				family,
				sync.equals(ServerSyncType.BUGLINK) ? "Synchronizing BugLink data for server '"
						+ server.getLabel() + "'"
						: "Synchronizing Sierra data for project '"
								+ projectName + "'", server, projectName,
				method);
		this.force = force;
		syncType = sync;
		setRule(DatabaseAccessRule.getInstance());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final int threshold = PreferenceConstants
				.getServerInteractionRetryThreshold();
		final int numProblems = f_server.getProblemCount()
				+ Projects.getInstance().getProblemCount(f_projectName);
		if (!force && (numProblems > threshold)) {
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
			if (joinJob != null && joinJob.troubleshoot(f_server)) {
				if (cause instanceof ServerMismatchException) {
					troubleshoot = new TroubleshootWrongServer(f_method,
							f_server, f_projectName);
				} else if (cause instanceof SierraServiceClientException) {
					troubleshoot = getTroubleshootConnection(f_method,
							(SierraServiceClientException) cause);
				}
				if (troubleshoot != null && troubleshoot.retry()) {
					troubleshoot.fix();
					if (troubleshoot.retry()) {
						return synchronize(sync, slMonitor);
					}
				}
			}
			joinJob.fail(f_server);
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
			SettingQueries.updateServerInfo(f_server.getServer()).perform(q);
			if (syncType.syncBugLink() && joinJob != null
					&& joinJob.process(f_server)) {
				final SierraServerLocation loc = f_server.getServer();
				SettingQueries.retrieveCategories(loc,
						SettingQueries.categoryRequest().perform(q)).perform(q);
				SettingQueries.retrieveScanFilters(loc,
						SettingQueries.scanFilterRequest().perform(q)).perform(
						q);
				slMonitor.worked(1);
			}
			if (syncType.syncProjects()) {
				ClientProjectManager.getInstance(conn).synchronizeProject(
						f_server.getServer(), f_projectName, slMonitor);
			}
			f_server.markAsConnected();
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
		final String msg = I18N.err(51, f_projectName, f_server);
		SLLogger.getLogger().log(Level.WARNING, msg, e);
		return Status.CANCEL_STATUS;
	}
}
