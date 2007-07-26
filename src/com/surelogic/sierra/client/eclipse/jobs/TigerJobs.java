package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class TigerJobs extends Job {

	String toolName = null;

	@Override
	public boolean belongsTo(Object family) {
		return "Tiger".equals(family);
	}

	public TigerJobs(String familyName, String toolName) {
		super(toolName);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

}