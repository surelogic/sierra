package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
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
		final ServerProjectGroupJob joinJob = new ServerProjectGroupJob(manager
				.getServers().toArray(ServerProjectGroupJob.NO_SERVERS));

		for (String projectName : manager.getConnectedProjects()) {
			final SierraServer server = manager.getServer(projectName);

			final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
				public void run(String projectName, SierraServer server,
						Shell shell) {
					final SynchronizeJob job = new SynchronizeJob(joinJob,
							projectName, server);
					job.schedule();
				}
			};
			final Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell();
			ServerAuthenticationDialog.promptPasswordIfNecessary(projectName,
					server, shell, serverAction);
			joinJob.schedule();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
}
