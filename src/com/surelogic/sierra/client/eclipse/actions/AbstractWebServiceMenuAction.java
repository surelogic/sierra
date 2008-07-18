package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;

public abstract class AbstractWebServiceMenuAction extends
		AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {

		final SierraServerManager manager = SierraServerManager.getInstance();
		SierraServer unconnectedProjectsServer = null;
		final Shell shell = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell();
		final ServerProjectGroupJob family = 
			new ServerProjectGroupJob(manager.getServers());
		final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
			public void run(String projectName, SierraServer server, Shell shell) {
				runServerAction(family, projectName, server, shell);
			}
		};
		runningOnProjects(projectNames);

		for (String projectName : projectNames) {
			/*
			 * Is the project connected to a server?
			 */
			if (manager.isConnected(projectName)) {
				/*
				 * Yes, start the job.
				 */
				final SierraServer server = manager.getServer(projectName);
				ServerAuthenticationDialog.promptPasswordIfNecessary(
						projectName, server, shell, serverAction);
			} else {
				/*
				 * Are any servers defined?
				 */
				if (manager.isEmpty()) {
					final int errNo = 17;
					final String msg = I18N.err(errNo);
					final IStatus reason = SLEclipseStatusUtility.createErrorStatus(17, msg);
					ErrorDialogUtility.open(shell, "No Sierra Servers", reason);
					ViewUtility.showView(SierraServersView.ID);
					ServerLocationDialog.newServer(shell);
					return;
				}

				final SierraServer server;
				if (unconnectedProjectsServer == null) {
					/*
					 * Select a server to connect this project to.
					 */
					ServerSelectionDialog dialog = new ServerSelectionDialog(
							shell, projectName);
					if (dialog.open() == Window.CANCEL) {
						/*
						 * Just stop
						 */
						return;
					}
					server = dialog.getServer();
					if (!dialog.confirmNonnullServer()) {
						return;
					}
					if (dialog.useForAllUnconnectedProjects())
						unconnectedProjectsServer = server;
				} else {
					server = unconnectedProjectsServer;
				}

				manager.connect(projectName, server);

				ServerAuthenticationDialog.promptPasswordIfNecessary(
						projectName, server, shell, serverAction);
			}
		}
		family.schedule();
	}

	void runningOnProjects(List<String> projectNames) {
		// Nothing to do
	}
	
	abstract void runServerAction(final ServerProjectGroupJob family,
			final String projectName, final SierraServer server,
			final Shell shell);
}
