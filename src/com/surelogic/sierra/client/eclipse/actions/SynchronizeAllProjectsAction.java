package com.surelogic.sierra.client.eclipse.actions;

import java.util.concurrent.atomic.AtomicLong;

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
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public final class SynchronizeAllProjectsAction implements
		IWorkbenchWindowActionDelegate {
	private final boolean force;
	private ServerProjectGroupJob group;
  private final ServerFailureReport f_method;

	public SynchronizeAllProjectsAction(boolean force) {
		this(ServerFailureReport.SHOW_DIALOG, force);
	}
	
	public SynchronizeAllProjectsAction(ServerFailureReport method, boolean force) {
	  f_method = method;
	  this.force = force;
	}
	
	public SynchronizeAllProjectsAction() {
		this(true);
	}
	
    private static final AtomicLong lastSyncTime = 
    	new AtomicLong(System.currentTimeMillis());
	
    public static AtomicLong getLastSyncTime() {
    	return lastSyncTime;
    }
    
    public static void setTime() {
    	long now = System.currentTimeMillis();
		lastSyncTime.set(now);
    }
    
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

		setTime();
		
		for (String projectName : manager.getConnectedProjects()) {
			final SierraServer server = manager.getServer(projectName);

			final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
				public void run(String projectName, SierraServer server,
						Shell shell) {
					final SynchronizeJob job = new SynchronizeJob(joinJob,
							projectName, server, force, f_method);
					job.schedule();
				}
			};
			// FIX 
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			final Shell shell = window == null? null : window.getShell();
			ServerAuthenticationDialog.promptPasswordIfNecessary(projectName,
					server, shell, serverAction);
		}
		group = joinJob;
		joinJob.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}
	
	public ServerProjectGroupJob getGroup() {
		return group;
	}
}
