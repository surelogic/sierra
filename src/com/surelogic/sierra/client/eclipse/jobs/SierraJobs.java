package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * TODO: Consider dumping this class or being more clear what the design intent
 * is?
 */
public class SierraJobs extends Job {

	public static final String SIERRA = "Sierra";

	@Override
	public boolean belongsTo(Object family) {
		return SIERRA.equals(family);
	}

	public SierraJobs(String familyName, String toolName) {
		super(toolName);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}
}