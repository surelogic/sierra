package com.surelogic.sierra.client.eclipse.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public class SynchronizeBugLinkServerAction implements IWorkbenchWindowActionDelegate {
	private final boolean force;
	private final ServerFailureReport f_method;
	private SierraServer f_server;
	
	public SynchronizeBugLinkServerAction(ServerFailureReport method, boolean force) {
		f_method = method;
		this.force = force;
	}

	public void dispose() {
		// FIX what to do?
	}

	public void init(IWorkbenchWindow window) {
		// FIX what to do?
	}

	public void run(IAction action) {
		final SynchronizeJob job = 
			new SynchronizeJob(null, null, f_server,
				               ServerSyncType.BUGLINK, force, f_method);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// FIX what to do?
	}
	
	public void run(SierraServer server) {
		f_server = server;
		run((IAction) null);
	}
}
