package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
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

	public SynchronizeJob(ServerProjectGroupJob family, String projectName,
			SierraServer server, ServerSyncType sync, boolean force,
			ServerFailureReport method) {
		super(
				family,
				sync.equals(ServerSyncType.BUGLINK) ? "Synchronizing BugLink data for server '"
						+ server.getLabel() + "'"
						: "Synchronizing Sierra data for project '"
								+ projectName + "'", server, projectName,
				method);
		this.force = force;
		syncType = sync;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
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
		IStatus status = null;
		try {
			final Connection conn = Data.getInstance().transactionConnection();
			try {
				status = synchronize(conn, slMonitor);
			} catch (final Throwable e) {
				status = createWarningStatus(51, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (final SQLException e1) {
			if (status == null) {
				status = createWarningStatus(51, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus synchronize(Connection conn, SLProgressMonitor slMonitor)
			throws SQLException {
		TroubleshootConnection troubleshoot;
		try {
			if (syncType.syncBugLink() && joinJob.process(f_server)) {
				final SierraServerLocation loc = f_server.getServer();
				final Query q = new ConnectionQuery(conn);
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
				conn.commit();
				DatabaseHub.getInstance().notifyServerSynchronized();
				return Status.OK_STATUS;
			}
		} catch (final ServerMismatchException e) {
			if (joinJob.troubleshoot(f_server)) {
				troubleshoot = new TroubleshootWrongServer(f_method, f_server,
						f_projectName);
				conn.rollback();
				troubleshoot.fix();
				if (troubleshoot.retry()) {
					return synchronize(conn, slMonitor);
				}
				joinJob.fail(f_server);
			}
			return fail(e);
		} catch (final SierraServiceClientException e) {
			if (joinJob.troubleshoot(f_server)) {
				troubleshoot = getTroubleshootConnection(f_method, e);
				conn.rollback();
				troubleshoot.fix();
				if (troubleshoot.retry()) {
					return synchronize(conn, slMonitor);
				}
				joinJob.fail(f_server);
			}
			return fail(e);
		}
	}

	private IStatus fail(Exception e) {
		final String msg = I18N.err(51, f_projectName, f_server);
		SLLogger.getLogger().log(Level.WARNING, msg, e);
		return Status.CANCEL_STATUS;
	}
}
