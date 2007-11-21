package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

public final class SynchronizeAllProjectsAction implements
		IWorkbenchWindowActionDelegate {

	public void dispose() {
		// Nothing to do
	}

	public void init(IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(IAction action) {

		final SierraServerManager manager = SierraServerManager.getInstance();
		for (String projectName : Projects.getInstance().getProjectNames()) {
			if (manager.isConnected(projectName)) {
				final SierraServer server = manager.getServer(projectName);
				promptPasswordIfNecessary(projectName, server, PlatformUI
						.getWorkbench().getDisplay().getActiveShell());
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
		final SynchronizeJob job = new SynchronizeJob(projectName, server);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
