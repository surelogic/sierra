package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.scan.ScanQueries;

public class DeleteUnfinishedScans extends DatabaseJob {

	public DeleteUnfinishedScans() {
		super("Deleting unfinished scans.");
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final SLProgressMonitor mon = new SLProgressMonitorWrapper(monitor,
				"Removing any scans that may have not finished prepping.");
		mon.begin();
		Data.getInstance().withTransaction(
				ScanQueries.deleteUnfinishedScans(mon));
		mon.done();
		return Status.OK_STATUS;
	}

}
