package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.sierra.client.eclipse.model.Projects;

public final class DeleteProjectDataJob extends Job {

	private final String f_projectName;

	public DeleteProjectDataJob(final String projectName) {
		super("Deleting Sierra data for project '" + projectName + "'");
		f_projectName = projectName;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		System.out.println("run delete " + f_projectName);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Projects.getInstance().refresh();
		return Status.OK_STATUS;
	}

}
