package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;
import java.util.logging.Level;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerLocationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerSelectionDialog;
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

		for (String projectName : projectNames) {
			/*
			 * Is the project connected to a server?
			 */
			if (manager.isConnected(projectName)) {
				/*
				 * Yes, start the job.
				 */
				final SierraServer server = manager.getServer(projectName);
				promptPasswordIfNecessary(projectName, server, shell);
			} else {
				/*
				 * Are any servers defined?
				 */
				if (manager.isEmpty()) {
					final StringBuilder b = new StringBuilder();
					b.append("There are no Sierra server locations defined. ");
					b.append("A project must be connected to a Sierra ");
					b.append("server to perform this action. ");
					b.append("The 'Sierra Team Server' view will be ");
					b.append("opened so that you can define a location. ");
					b.append("Invoke this action again once you have ");
					b.append("defined a Sierra server location.");
					MessageDialog.openError(shell, "No Sierra Servers", b
							.toString());
					ViewUtility.showView(SierraServersView.class.getName());
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
					if (server == null) {
						SLLogger
								.getLogger()
								.log(Level.SEVERE,
										"null Sierra server returned from ServerSelectionDialog (bug).");
						return;
					}
					if (dialog.useForAllUnconnectedProjects())
						unconnectedProjectsServer = server;
				} else {
					server = unconnectedProjectsServer;
				}

				manager.connect(projectName, server);

				promptPasswordIfNecessary(projectName, server, shell);
			}
		}
	}

	private void promptPasswordIfNecessary(final String projectName,
			final SierraServer server, final Shell shell) {
		if (!server.savePassword() && !server.usedToConnectToAServer()) {
			ServerAuthenticationDialog dialog = new ServerAuthenticationDialog(
					shell, server);
			if (dialog.open() == Window.CANCEL) {
				/*
				 * Just stop, don't try to run the job.
				 */
				return;
			}
			server.setUsed(); // for this Eclipse session
		}
		run(projectName, server, shell);
	}

	abstract void run(final String projectName, final SierraServer server,
			final Shell shell);
}
