package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public final class DeleteDatabaseJob extends DatabaseJob {

	public DeleteDatabaseJob() {
		super("Deleting Sierra database.");
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String msg = "Deleting the database from the file system.";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin();
		Data.getInstance().destroy();
		DatabaseHub.getInstance().notifyDatabaseDeleted();
		SLLogger.getLogger().info("The client database has been deleted");
		return Status.OK_STATUS;
	}
}
