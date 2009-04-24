package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public final class RefreshDatabaseJob extends AbstractSLJob {

	public RefreshDatabaseJob() {
		super("Refreshing Sierra database.");
	}

	public SLStatus run(SLProgressMonitor monitor) {
		//final String msg = "Refreshing the database from the file system.";
		monitor.begin();
		ConnectedServerManager.getInstance().changed();
		// Projects.getInstance().clear();
		Data.getInstance().loggedBootAndCheckSchema();
		DatabaseHub.getInstance().notifyDatabaseDeleted(); // TODO
		SLLogger.getLogger().info("The client database has been refreshed");
		return SLStatus.OK_STATUS;
	}
}
