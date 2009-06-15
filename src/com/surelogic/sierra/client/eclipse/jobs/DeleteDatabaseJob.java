package com.surelogic.sierra.client.eclipse.jobs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

public final class DeleteDatabaseJob extends AbstractSierraDatabaseJob {

	public DeleteDatabaseJob() {
		super("Deleting Sierra database.");
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String msg = "Deleting the database from the file system.";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin();
		ConnectedServerManager.getInstance().clear();
		// Projects.getInstance().clear();
		Data.getInstance().destroy();
		try {
			Data.getInstance().bootAndCheckSchema();
		} catch (Exception e) {
			SLLogger.getLogger().log(Level.SEVERE, "Unable to re-boot database", e);
			return Status.CANCEL_STATUS; // FIX
		}
		DatabaseHub.getInstance().notifyDatabaseDeleted();
		SLLogger.getLogger().info("The client database has been deleted");
		return Status.OK_STATUS;
	}
}
