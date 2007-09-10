package com.surelogic.sierra.client.eclipse.jobs;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServerModel;
import com.surelogic.sierra.jdbc.project.ProjectManager;
import com.surelogic.sierra.tool.message.SierraServer;

public class SynchronizeProjectDataJob extends DatabaseJob {

	private final List<String> f_projectNames;
	private final SierraServer f_server;

	public SynchronizeProjectDataJob(List<String> projectNames,
			SierraServerModel server) {
		super("Deleting Sierra data for projects '" + projectNames + "'");
		f_projectNames = projectNames;
		f_server = server.getServer();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Deleting selected projects",
				f_projectNames.size() * 4);
		boolean jobFailed = false;
		try {
			final Connection conn = Data.getConnection();

			conn.setAutoCommit(false);
			final ProjectManager manager = ProjectManager.getInstance(conn);
			try {
				for (final String projectName : f_projectNames) {
					manager
							.synchronizeProject(f_server, projectName,
									slMonitor);
					conn.commit();
				}
			} catch (Exception e) {
				final String msg = "Deletion of Sierra data about projects "
						+ f_projectNames + " failed.";
				SLLogger.getLogger().log(Level.SEVERE, msg, e);
				jobFailed = true;
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			final String msg = "Deletion of Sierra data about projects "
					+ f_projectNames + " failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e1);
			jobFailed = true;
		}
		return null;
		
	}

}
