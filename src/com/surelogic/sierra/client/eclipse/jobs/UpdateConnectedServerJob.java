package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class UpdateConnectedServerJob extends AbstractServerProjectJob {

	public UpdateConnectedServerJob(final ServerProjectGroupJob family,
			final String name, final ConnectedServer server,
			final String project, final ServerFailureReport method) {
		super(family, name, server, project, method);

	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
