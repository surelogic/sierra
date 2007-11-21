package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
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
				final Shell shell = PlatformUI.getWorkbench().getDisplay()
						.getActiveShell();
				final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
					public void run(String projectName, SierraServer server,
							Shell shell) {
						final SynchronizeJob job = new SynchronizeJob(
								projectName, server);
						job.schedule();
					}
				};
				ServerAuthenticationDialog.promptPasswordIfNecessary(
						projectName, server, shell, serverAction);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
