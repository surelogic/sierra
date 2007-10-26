package com.surelogic.sierra.client.eclipse.jobs;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
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
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.jdbc.project.ClientProjectManager;

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
						final ClientProjectManager manager = ClientProjectManager
								.getInstance(conn);
						try {
							for (final String projectName : f_projectNames) {
								manager.deleteProject(projectName, slMonitor);
								conn.commit();
								SierraServerManager.getInstance().disconnect(
										projectName);
								DatabaseHub.getInstance()
										.notifyProjectDeleted();
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
					if (jobFailed) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										final MessageBox dialog = new MessageBox(
												shell, SWT.ICON_ERROR | SWT.OK);
										dialog.setText("Failure");
										dialog
												.setMessage("Deletion of Sierra data about projects "
														+ f_projectNames
														+ " failed. All exceptions have been logged to the Eclipse 'Error Log'");
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

	public static void utility(List<String> projectNames, Shell shell,
			boolean disconnect) {
		if (shell == null) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
		}
		final boolean multiDelete = projectNames.size() > 1;

		final String title = "Confirm "
				+ (multiDelete ? "Multiple Project" : "Project")
				+ (disconnect ? " Disconnect" : " Sierra Data Deletion");
		final StringBuilder b = new StringBuilder();
		b.append("Disconnecting from the Sierra Team ");
		b.append("Server will automatically delete ");
		b.append("data in your Eclipse workspace about ");
		if (multiDelete) {
			b.append("these ").append(projectNames.size()).append(" projects,");
		} else {
			b.append("the project '").append(projectNames.get(0)).append("',");
		}
		b.append(" but will not change or delete any information that ");
		b.append("you have already published to the server.\n\n");
		b.append("Are you should that you want to disconnect?");
		if (MessageDialog.openConfirm(shell, title, b.toString())) {
			/*
			 * Because this job can be run from a modal dialog we need to manage
			 * showing its progress ourselves. Therefore, this job is not a
			 * typical workspace job.
			 */
			final DeleteProjectDataJob job = new DeleteProjectDataJob(
					projectNames);
			job.runModal(shell);
		}
	}
}
