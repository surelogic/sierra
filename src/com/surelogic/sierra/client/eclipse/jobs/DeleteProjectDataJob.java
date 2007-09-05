package com.surelogic.sierra.client.eclipse.jobs;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.jdbc.project.ProjectManager;

public final class DeleteProjectDataJob {

	private final List<String> f_projectNames;

	public DeleteProjectDataJob(final List<String> projectNames) {
		f_projectNames = projectNames;
	}

	public void runModal(final Shell shell) {
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					for (final String projectName : f_projectNames) {
						try {
							Connection conn = Data.getConnection();

							try {
								ProjectManager.getInstance(conn).deleteProject(
										projectName,
										new SLProgressMonitorWrapper(monitor));
								conn.commit();
							} finally {
								conn.close();
							}
						} catch (Exception e) {
							final String msg = "Deletion of Sierra data about project '"
									+ projectName + "' failed.";
							SLLogger.getLogger().log(Level.SEVERE, msg, e);
						}
					}
					DatabaseHub.getInstance().notifyProjectDeleted();
				}
			});
		} catch (Exception e) {
			final String msg = "Deletion of Sierra data about projects "
					+ f_projectNames + " failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
		}

	}
}
