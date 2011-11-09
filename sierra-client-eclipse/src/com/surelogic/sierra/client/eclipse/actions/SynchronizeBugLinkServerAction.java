package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class SynchronizeBugLinkServerAction implements
		IWorkbenchWindowActionDelegate {
	private final boolean force;
	private final ServerFailureReport f_strategy;
	private ConnectedServer f_server;

	public SynchronizeBugLinkServerAction(final ServerFailureReport strategy,
			final boolean force) {
		f_strategy = strategy;
		this.force = force;
	}

	public void dispose() {
		// FIX what to do?
	}

	public void init(final IWorkbenchWindow window) {
		// FIX what to do?
	}

	public void run(final IAction action) {
		final SynchronizeJob job = new SynchronizeJob(null, f_server, force,
				f_strategy);
		job.schedule();
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		// FIX what to do?
	}

	public void run(final ConnectedServer server) {
		f_server = server;
		run((IAction) null);
	}
}
