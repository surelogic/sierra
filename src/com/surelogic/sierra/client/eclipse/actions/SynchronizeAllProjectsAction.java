package com.surelogic.sierra.client.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog.ServerActionOnAProject;
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
		final List<SynchronizeJob> jobs = new ArrayList<SynchronizeJob>();
		final Job joinJob = new Job("Waiting for synchronize jobs") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          Job.getJobManager().join(this, monitor);
        } catch (OperationCanceledException e) {
          e.printStackTrace();
          return Status.CANCEL_STATUS;
        } catch (InterruptedException e) {
          e.printStackTrace();
          return Status.CANCEL_STATUS;
        }          
        for(SynchronizeJob j : jobs) {
          System.out.println(j.getResult());
        }
        return Status.OK_STATUS;
      }
		};
		joinJob.setSystem(true);
		
		for (String projectName : SierraServerManager.getInstance()
				.getConnectedProjects()) {
			final SierraServer server = manager.getServer(projectName);

			final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
				public void run(String projectName, SierraServer server,
						Shell shell) {
					final SynchronizeJob job = new SynchronizeJob(joinJob, projectName,
							server);
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
