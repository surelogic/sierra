package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.views.SierraServersView;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public abstract class AbstractWebServiceMenuAction extends
		AbstractProjectSelectedMenuAction {

	@Override
	protected void run(List<IJavaProject> selectedProjects,
			List<String> projectNames) {

		final ConnectedServerManager manager = ConnectedServerManager
				.getInstance();
		ConnectedServer unconnectedProjectsServer = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final Shell shell = window == null ? null : window.getShell();
		final ServerProjectGroupJob family = new ServerProjectGroupJob(manager
				.getServers());
		final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
			@Override
			public void run(String projectName, ConnectedServer server,
					Shell shell) {
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
				final ConnectedServer server = manager.getServer(projectName);
				ServerActionOnAProject.promptPasswordIfNecessary(projectName,
						server, shell, serverAction);
			} else {
				/*
				 * Are any servers defined?
				 */
				if (manager.isEmpty()) {
					final int errNo = 17;
					final String msg = I18N.err(errNo);
					final IStatus reason = SLEclipseStatusUtility
							.createErrorStatus(17, msg);
					ErrorDialogUtility.open(shell, "No Sierra Servers", reason);
					ViewUtility.showView(SierraServersView.ID);
					ServerLocationDialog.newServer(shell);
					return;
				}

				final ConnectedServer server;
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

				ServerActionOnAProject.promptPasswordIfNecessary(projectName,
						server, shell, serverAction);
			}
		}
		family.schedule();
	}

	void runningOnProjects(List<String> projectNames) {
		// Nothing to do
	}

	abstract void runServerAction(final ServerProjectGroupJob family,
			final String projectName, final ConnectedServer server,
			final Shell shell);
}
