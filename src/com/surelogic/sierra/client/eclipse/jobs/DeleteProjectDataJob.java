package com.surelogic.sierra.client.eclipse.jobs;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.SLProgressMonitor;
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
					SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
							monitor);
					slMonitor.beginTask("Deleting selected projects",
							f_projectNames.size() * 4);

					boolean jobFailed = false;
					try {
						final Connection conn = Data.getConnection();

						conn.setAutoCommit(false);
						final ProjectManager manager = ProjectManager
								.getInstance(conn);
						try {
							for (final String projectName : f_projectNames) {
								manager.deleteProject(projectName, slMonitor);
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
					monitor.done();
					DatabaseHub.getInstance().notifyProjectDeleted();
					if (!jobFailed) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										final MessageBox dialog = new MessageBox(
												shell, SWT.ICON_ERROR | SWT.OK);
										dialog.setText("Failure");
										dialog
												.setMessage("Deletion of Sierra data about projects "
														+ f_projectNames
														+ " failed.");
										dialog.open();
									}
								});
					}
				}
			});
		} catch (Exception e) {
			final String msg = "Deletion of Sierra data about projects "
					+ f_projectNames + " failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
		}

	}
}
