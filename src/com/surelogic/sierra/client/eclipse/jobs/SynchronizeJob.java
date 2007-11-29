package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongServer;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public class SynchronizeJob extends DatabaseJob {

	private final String f_projectName;
	private final SierraServer f_server;

	public SynchronizeJob(String projectName, SierraServer server) {
		super("Synchronizing Sierra data from project '" + projectName + "'");
		f_projectName = projectName;
		f_server = server;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Synchronizing findings and settings for project "
				+ f_projectName + ".", 5);
		IStatus status = null;
		try {
			final Connection conn = Data.transactionConnection();
			try {
				final ClientProjectManager manager = ClientProjectManager
						.getInstance(conn);
				status = synchronize(conn, manager, slMonitor);
			} catch (Throwable e) {
				final String msg = "Synchronization of project '"
						+ f_projectName + "' to Sierra server '" + f_server
						+ "' failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e);
				status = SLStatus.createErrorStatus(msg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				final String msg = "Synchronization of project '"
						+ f_projectName + "' to Sierra server '" + f_server
						+ "' failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e1);
				status = SLStatus.createErrorStatus(msg, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus synchronize(Connection conn, ClientProjectManager manager,
			SLProgressMonitor slMonitor) throws SQLException {
		TroubleshootConnection troubleshoot;
		try {
			manager.synchronizeProject(f_server.getServer(), f_projectName,
					slMonitor);
			if (slMonitor.isCanceled()) {
				conn.rollback();
				return Status.CANCEL_STATUS;
			} else {
				conn.commit();
				DatabaseHub.getInstance().notifyServerSynchronized();
				return Status.OK_STATUS;
			}
		} catch (ServerMismatchException e) {
			troubleshoot = new TroubleshootWrongServer(f_server, f_projectName);
			conn.rollback();
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return synchronize(conn, manager, slMonitor);
			} else {
				SLLogger.getLogger().log(
						Level.WARNING,
						"Failed to synchronize " + f_projectName + " with "
								+ f_server + " (wrong server)", e);
				return Status.CANCEL_STATUS;
			}
		} catch (SierraServiceClientException e) {
			if (e instanceof InvalidLoginException) {
				troubleshoot = new TroubleshootWrongAuthentication(f_server,
						f_projectName);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_server,
						f_projectName);
			}
			conn.rollback();
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return synchronize(conn, manager, slMonitor);
			} else {
				SLLogger.getLogger().log(
						Level.WARNING,
						"Failed to synchronize " + f_projectName + " with "
								+ f_server, e);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
