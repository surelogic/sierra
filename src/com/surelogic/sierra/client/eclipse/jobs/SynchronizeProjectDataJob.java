package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;
import com.surelogic.sierra.jdbc.project.ProjectManager;
import com.surelogic.sierra.tool.message.SierraServer;

public class SynchronizeProjectDataJob extends DatabaseJob {

	private final String f_projectName;
	private final SierraServer f_server;

	public SynchronizeProjectDataJob(String projectName,
			SierraServerModel server) {
		super("Synchronizing Sierra data form project '" + projectName + "'");
		f_projectName = projectName;
		f_server = server.getServer();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Synchronizing findings and settings for project "
				+ f_projectName + ".", 5);
		boolean jobFailed = false;
		try {
			final Connection conn = Data.getConnection();
			conn.setAutoCommit(false);
			final ProjectManager manager = ProjectManager.getInstance(conn);
			try {
				manager.synchronizeProject(f_server, f_projectName, slMonitor);
				conn.commit();
			} catch (Exception e) {
				final String msg = "Synchronization of sierra project "
						+ f_projectName + " failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e);
				jobFailed = true;
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			final String msg = "Synchronization of sierra project "
					+ f_projectName + " failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e1);
			jobFailed = true;
		}

		return null;

	}
}
