package com.surelogic.sierra.client.eclipse.jobs;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.sierra.client.eclipse.Data;

/**
 * This job is used to disconnect the Sierra database under the data directory.
 */
public class DisconnectDatabase extends AbstractSLJob {
	public DisconnectDatabase() {
		super("Disconnect the Sierra database");
	}

	public SLStatus run(SLProgressMonitor monitor) {
		monitor.begin();
		try {
			/*
			 * Disconnect from the connected Sierra database.
			 */
			try {
				Data.getInstance().shutdown();
			} catch (final Exception e) {
				return SLStatus.createErrorStatus(e);
			}
		} finally {
			monitor.done();
		}
		return SLStatus.OK_STATUS;
	}
}
