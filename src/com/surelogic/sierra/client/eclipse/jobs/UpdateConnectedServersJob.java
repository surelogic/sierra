package com.surelogic.sierra.client.eclipse.jobs;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ServerLocations;

public class UpdateConnectedServersJob extends DatabaseJob {

	private final Map<ConnectedServer, Collection<String>> f_servers;

	public UpdateConnectedServersJob(
			final Map<ConnectedServer, Collection<String>> servers) {
		super("Updating the local connected servers.");
		f_servers = servers;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		try {
			Data.getInstance().withTransaction(
					ServerLocations.saveQuery(f_servers, false));
			return Status.OK_STATUS;
		} catch (final TransactionException e) {
			return SLEclipseStatusUtility.createErrorStatus(38, I18N.err(38,
					"Team Servers", "database"), e);
		}
	}
}
