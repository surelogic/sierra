package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.SynchronizeJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class SynchronizeProjectAction extends AbstractWebServiceMenuAction {
	@Override
	void run(String projectName, SierraServer server, Shell shell) {
		final SynchronizeJob job = new SynchronizeJob(
				projectName, server);
		job.schedule();
	}
}
