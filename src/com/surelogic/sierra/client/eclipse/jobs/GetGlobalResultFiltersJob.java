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
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class GetGlobalResultFiltersJob extends DatabaseJob {

	private final SierraServer f_server;

	public GetGlobalResultFiltersJob(SierraServer server) {
		super("Getting local settings from " + server.getLabel());
		f_server = server;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		final String msg = "Getting local settings from Sierra server '"
				+ f_server + "'";
		slMonitor.beginTask(msg, 5);
		IStatus status = null;
		try {
			final Connection conn = Data.transactionConnection();
			try {
				status = getResultFilters(conn, slMonitor);
			} catch (Throwable e) {
				SLLogger.getLogger().log(Level.SEVERE, msg + " failed.", e);
				status = SLStatus.createErrorStatus(msg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				SLLogger.getLogger().log(Level.SEVERE, msg + " failed.", e1);
				status = SLStatus.createErrorStatus(msg, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus getResultFilters(Connection conn,
			SLProgressMonitor slMonitor) throws SQLException {
		// TODO
		return null;
	}
}
