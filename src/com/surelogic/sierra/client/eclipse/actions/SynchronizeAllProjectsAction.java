package com.surelogic.sierra.client.eclipse.actions;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class SynchronizeAllProjectsAction implements
		IWorkbenchWindowActionDelegate {
	private final boolean force;
	private ServerProjectGroupJob group;
	private final ServerFailureReport f_method;
	private final ServerSyncType f_syncType;

	public SynchronizeAllProjectsAction(ServerSyncType sync,
			ServerFailureReport method, boolean force) {
		f_syncType = sync;
		f_method = method;
		this.force = force;
	}

	public SynchronizeAllProjectsAction() {
		this(ServerSyncType.ALL, ServerFailureReport.SHOW_DIALOG, true);
	}

	private static final AtomicLong lastSyncTime = new AtomicLong(System
			.currentTimeMillis());

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
		final ConnectedServerManager manager = ConnectedServerManager
				.getInstance();
		final ServerProjectGroupJob joinJob = new ServerProjectGroupJob(manager
				.getServers());

		setTime();

		final Set<ConnectedServer> unconnectedServers = manager.getServers();
		if (f_syncType.syncProjects()) {
			for (String projectName : manager.getConnectedProjects()) {
				final ConnectedServer server = manager.getServer(projectName);
				unconnectedServers.remove(server);

				if (f_syncType.syncByServerSettings()
						&& !server.getLocation().isAutoSync()) {
					continue;
				}
				final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
					@Override
					public void run(String projectName, ConnectedServer server,
							Shell shell) {
						final SynchronizeJob job = new SynchronizeJob(joinJob,
								projectName, server, f_syncType, force,
								f_method);
						job.schedule();
					}
				};
				promptAndSchedule(projectName, server, serverAction);
			}
		}
		if (f_syncType.syncBugLink()) {
			for (ConnectedServer server : unconnectedServers) {
				final ServerActionOnAProject serverAction = new ServerActionOnAProject() {
					@Override
					public void run(String projectName, ConnectedServer server,
							Shell shell) {
						final SynchronizeJob job = new SynchronizeJob(joinJob,
								null, server, ServerSyncType.BUGLINK, force,
								f_method);
						job.schedule();
					}
				};
				promptAndSchedule(null, server, serverAction);
			}
		}
		group = joinJob;
		joinJob.schedule();
	}

	private void promptAndSchedule(String projectName, ConnectedServer server,
			ServerActionOnAProject serverAction) {
		ServerActionOnAProject.promptPasswordIfNecessary(projectName, server,
				SWTUtility.getShell(), serverAction);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to do
	}

	public ServerProjectGroupJob getGroup() {
		return group;
	}
}
