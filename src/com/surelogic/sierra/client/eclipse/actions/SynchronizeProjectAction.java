package com.surelogic.sierra.client.eclipse.actions;

import java.util.*;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.*;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;

public class SynchronizeProjectAction extends
		AbstractWebServiceMenuAction {
	@Override
	void runningOnProjects(List<String> projectNames) {
		Set<String> connected = ConnectedServerManager.getInstance().getConnectedProjects();
		if (projectNames.containsAll(connected)) {
			SynchronizeAllProjectsAction.setTime();
		}
	}
	
	@Override
	void runServerAction(ServerProjectGroupJob family, String projectName,
			SierraServer server, Shell shell) {
		final SynchronizeJob job = new SynchronizeJob(family, projectName,
				server, ServerSyncType.ALL, true, ServerFailureReport.SHOW_DIALOG);
		job.schedule();
	}
}
