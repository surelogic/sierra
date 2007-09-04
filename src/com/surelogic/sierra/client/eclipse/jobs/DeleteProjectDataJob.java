package com.surelogic.sierra.client.eclipse.jobs;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.adhoc.DatabaseJob;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.jdbc.project.ProjectManager;

public final class DeleteProjectDataJob extends DatabaseJob {

	private final String f_projectName;

	public DeleteProjectDataJob(final String projectName) {
		super("Deleting Sierra data for project '" + projectName + "'");
		f_projectName = projectName;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			ProjectManager.getInstance(Data.getConnection()).deleteProject(
					f_projectName, new SLProgressMonitorWrapper(monitor));
		} catch (Exception e) {
			final String msg = "Deletion of Sierra data about project '"
					+ f_projectName + "' failed.";
			SLLogger.getLogger().log(Level.SEVERE, msg, e);
			return SLStatus.createErrorStatus(msg, e);
		}
		Projects.getInstance().refresh();
		return Status.OK_STATUS;
	}
}
