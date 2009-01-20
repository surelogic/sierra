package com.surelogic.sierra.client.eclipse.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.SettingQueries;

public class UpdateConnectedServerJob extends AbstractServerProjectJob {

	private final boolean f_savePassword;

	public UpdateConnectedServerJob(final ConnectedServer server,
			final boolean savePassword, final ServerFailureReport method) {
		super(null, "Updating connection parameters to " + server.getName(),
				server, null, method);
		f_savePassword = savePassword;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		try {
			Data.getInstance().withTransaction(
					SettingQueries.updateServerLocation(getServer(),
							f_savePassword));
			return Status.OK_STATUS;
		} catch (final Exception e) {
			return createErrorStatus(51, e);
		}
	}

}
