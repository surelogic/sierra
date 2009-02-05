package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeProjectsJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class SynchronizeAllProjectsAction implements
		IWorkbenchWindowActionDelegate {
	private final boolean force;
	private ServerProjectGroupJob group;
	private final ServerFailureReport f_strategy;
	private final ServerSyncType f_syncType;
	private final int delayInSec;

	public SynchronizeAllProjectsAction(final ServerSyncType sync,
			final ServerFailureReport strategy, final boolean force,
			final int delay) {
		f_syncType = sync;
		f_strategy = strategy;
		this.force = force;
		delayInSec = delay;
	}

	public SynchronizeAllProjectsAction() {
		this(ServerSyncType.ALL, ServerFailureReport.SHOW_DIALOG, true, 0);
	}

	private static final AtomicLong lastSyncTime = new AtomicLong(System
			.currentTimeMillis());

	public static AtomicLong getLastSyncTime() {
		return lastSyncTime;
	}

	public static void setTime() {
		final long now = System.currentTimeMillis();
		lastSyncTime.set(now);
	}

	public void dispose() {
		// Nothing to do
	}

	public void init(final IWorkbenchWindow window) {
		// Nothing to do
	}

	public void run(final IAction action) {
		final ConnectedServerManager manager = ConnectedServerManager
				.getInstance();
		final ServerProjectGroupJob joinJob = new ServerProjectGroupJob(manager
				.getServers());

		setTime();

		int totalDelay = 0;
		for (final ConnectedServer server : manager.getServers()) {
			final int delay = totalDelay;
			final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
				@Override
				public void run(final String projectName,
						final ConnectedServer server, final Shell shell) {
					final SynchronizeJob job = new SynchronizeJob(joinJob,
							server, force, f_strategy);
					job.schedule(delay);
				}
			};
			promptAndSchedule(null, server, serverAction);
			totalDelay += delayInSec;
			if (f_syncType.syncProjects()
					&& (server.getLocation().isAutoSync() || !f_syncType
							.syncByServerSettings())) {
				final List<String> projectsConnectedTo = manager
						.getProjectsConnectedTo(server);
				if (!projectsConnectedTo.isEmpty()) {
					final ServerActionOnAProject serverProjectsAction = new ServerActionOnAProject() {
						@Override
						public void run(final String projectName,
								final ConnectedServer server, final Shell shell) {
							final SynchronizeProjectsJob job = new SynchronizeProjectsJob(
									joinJob, server, projectsConnectedTo,
									force, f_strategy);
							job.schedule(delay);
						}
					};
					promptAndSchedule(null, server, serverProjectsAction);
					totalDelay += delayInSec;
				}
			}
		}
		group = joinJob;
		joinJob.schedule();
	}

	private void promptAndSchedule(final String projectName,
			final ConnectedServer server,
			final ServerActionOnAProject serverAction) {
		ServerActionOnAProject.promptPasswordIfNecessary(projectName, server,
				SWTUtility.getShell(), serverAction);
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		// Nothing to do
	}

	public ServerProjectGroupJob getGroup() {
		return group;
	}
}
