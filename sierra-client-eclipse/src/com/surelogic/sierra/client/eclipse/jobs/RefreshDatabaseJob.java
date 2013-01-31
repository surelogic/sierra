package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;

public final class RefreshDatabaseJob extends AbstractSLJob {

	public RefreshDatabaseJob() {
		super("Refreshing Sierra database.");
	}

	@Override
  public SLStatus run(SLProgressMonitor monitor) {
		//final String msg = "Refreshing the database from the file system.";
		monitor.begin();
		
		// It seems like I don't need to do anything
		/*
		ConnectedServerManager.getInstance().changed();
		// Projects.getInstance().clear();
		Data.getInstance().loggedBootAndCheckSchema();
		DatabaseHub.getInstance().notifyServerSynchronized(); // TODO fix
		SLLogger.getLogger().info("The client database has been refreshed");
		*/
		return SLStatus.OK_STATUS;
	}
}
