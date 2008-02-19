package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
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
				status = synchronize(conn, slMonitor);
			} catch (Throwable e) {
				final int errNo = 51;
				final String msg = I18N.err(errNo, f_projectName, f_server);
				status = SLStatus.createWarningStatus(errNo, msg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				final int errNo = 51;
				final String msg = I18N.err(errNo, f_projectName, f_server);
				status = SLStatus.createWarningStatus(errNo, msg, e1);
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
			ClientProjectManager.getInstance(conn).synchronizeProject(
					f_server.getServer(), f_projectName, slMonitor);
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
				return synchronize(conn, slMonitor);
			} else {
				final String msg = I18N.err(51, f_projectName, f_server);
				SLLogger.getLogger().log(Level.WARNING, msg, e);
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
				return synchronize(conn, slMonitor);
			} else {
				final String msg = I18N.err(51, f_projectName, f_server);
				SLLogger.getLogger().log(Level.WARNING, msg, e);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
