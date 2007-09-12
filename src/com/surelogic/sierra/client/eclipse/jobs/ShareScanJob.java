package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
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

public class ShareScanJob extends DatabaseJob {

	private final String f_projectName;
	private final SierraServerLocation f_server;
	private final File f_scanFile;

	public ShareScanJob(String projectName, SierraServer server, File scanFile) {
		super("Sharing scan of project '" + projectName + "'");
		f_projectName = projectName;
		f_server = server.getServer();
		f_scanFile = scanFile;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Sharing scan of project " + f_projectName + " to "
				+ f_server.getLabel() + ".", 5);
		IStatus status = null;
		try {
			final Connection conn = Data.getConnection();
			conn.setAutoCommit(false);
			final ProjectManager manager = ProjectManager.getInstance(conn);
			try {
				// manager.synchronizeProject(f_server, f_projectName,
				// slMonitor);
				if (monitor.isCanceled()) {
					conn.rollback();
					status = Status.CANCEL_STATUS;
				} else {
					conn.commit();
				}
			} catch (Exception e) {
				final String msg = "Sharing scan of project '" + f_projectName
						+ "' to Sierra server '" + f_server + "' failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e);
				status = SLStatus.createErrorStatus(msg, e);
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				final String msg = "Sharing scan of project '" + f_projectName
						+ "' to Sierra server '" + f_server + "' failed.";
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
