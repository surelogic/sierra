package com.surelogic.sierra.client.eclipse.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeProjectJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class SynchronizeProjectAction extends AbstractWebServiceMenuAction {
	@Override
	void runningOnProjects(final List<String> projectNames) {
		final Set<String> connected = ConnectedServerManager.getInstance()
				.getConnectedProjects();
		if (projectNames.containsAll(connected)) {
			SynchronizeAllProjectsAction.setTime();
		}
	}

	@Override
	void runServerAction(final ServerProjectGroupJob family,
			final String projectName, final ConnectedServer server,
			final Shell shell) {
		final SynchronizeJob job = new SynchronizeJob(family, server, true,
				ServerFailureReport.SHOW_DIALOG);
		final SynchronizeProjectJob job2 = new SynchronizeProjectJob(family,
				projectName, server, true, ServerFailureReport.SHOW_DIALOG);
		job.schedule();
		job2.schedule();
	}
}
