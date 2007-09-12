package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.jdbc.project.ProjectManager;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class SynchronizeJob extends DatabaseJob {

	private final String f_projectName;
	private final SierraServerLocation f_server;

	public SynchronizeJob(String projectName, SierraServer server) {
		super("Synchronizing Sierra data from project '" + projectName + "'");
		f_projectName = projectName;
		f_server = server.getServer();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Synchronizing findings and settings for project "
				+ f_projectName + " to " + f_server.getLabel() + ".", 5);
		IStatus status = null;
		try {
			final Connection conn = Data.getConnection();
			conn.setAutoCommit(false);
			final ProjectManager manager = ProjectManager.getInstance(conn);
			try {
				manager.synchronizeProject(f_server, f_projectName, slMonitor);
				if (monitor.isCanceled()) {
					conn.rollback();
					status = Status.CANCEL_STATUS;
				} else {
					conn.commit();
				}
			} catch (Exception e) {
				final String msg = "Synchronization of project '"
						+ f_projectName + "' to Sierra server '" + f_server
						+ "' failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e);
				status = SLStatus.createErrorStatus(msg, e);
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
}
