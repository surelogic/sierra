package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.sierra.client.eclipse.jobs.SynchronizeProjectDataJob;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class SynchronizeAction extends AbstractWebServiceMenuAction {
	@Override
	void run(String projectName, SierraServer server, Shell shell) {
		SynchronizeProjectDataJob job = new SynchronizeProjectDataJob(
				projectName, server);
		job.schedule();
	}
}
