package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;
import java.util.logging.Level;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeProjectDataJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

public final class SynchronizeAction extends AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {

		final SierraServerManager manager = SierraServerManager.getInstance();
		final Shell shell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();

		for (String projectName : projectNames) {
			if (manager.isConnected(projectName)) {
				final SierraServer server = manager.getServer(projectName);
				SynchronizeProjectDataJob job = new SynchronizeProjectDataJob(
						projectName, server);
				job.schedule();
			} else {
				/*
				 * Are any servers defined?
				 */
				if (manager.isEmpty()) {
					final MessageBox confirmDelete = new MessageBox(shell,
							SWT.ICON_ERROR | SWT.APPLICATION_MODAL | SWT.OK);
					confirmDelete.setText("Synchronize Explanations Failed");
					confirmDelete
							.setMessage("There are no Sierra server locations defined. "
									+ "A Sierra server location is required to perform this action. "
									+ "The 'Sierra Server' view will be opened so that you can define a location. "
									+ "Invoke this action again once a Sierra server location is defined.");

					confirmDelete.open();
					ViewUtility.showView(SierraServersView.class.getName());
					return;
				}
				/*
				 * Select a server to connect this project to.
				 */
				ServerSelectionDialog dialog = new ServerSelectionDialog(shell,
						projectName);
				if (dialog.open() == Window.CANCEL) {
					/*
					 * Just stop
					 */
					return;
				}
				final SierraServer server = dialog.getServer();
				if (server == null) {
					SLLogger
							.getLogger()
							.log(Level.SEVERE,
									"null Sierra server returned from ServerSelectionDialog (bug).");
					return;
				}
				SynchronizeProjectDataJob job = new SynchronizeProjectDataJob(
						projectName, server);
				job.schedule();
			}
		}
	}
}
