package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

/**
 * This job is used to boot the Sierra database under the data directory.
 */
public class BootDatabase extends AbstractSLJob {
	public BootDatabase() {
		super("Boot the Sierra database");
	}

	public SLStatus run(SLProgressMonitor monitor) {
		monitor.begin();
		try {
			/*
			 * Disconnect from the connected Sierra database.
			 */
			try {
				Data.getInstance().bootAndCheckSchema();				
				System.out.println("Booted");
				
				DatabaseHub.getInstance().notifyFindingMutated();
				DatabaseHub.getInstance().notifyScanLoaded();
				DatabaseHub.getInstance().notifyDatabaseDeleted();
				Projects.getInstance().changed();
				ConnectedServerManager.getInstance().changed();
				BuglinkData.getInstance().changed();
				SelectionManager.getInstance().load(Activator.getDefault().getSelectionSaveFile());				
			} catch (final Exception e) {
				return SLStatus.createErrorStatus(e);
			}
		} finally {
			monitor.done();
		}
		return SLStatus.OK_STATUS;
	}
}
