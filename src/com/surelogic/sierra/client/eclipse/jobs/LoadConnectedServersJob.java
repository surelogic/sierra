package com.surelogic.sierra.client.eclipse.jobs;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.jdbc.settings.ServerLocations;

/**
 * This job looks up the existing connected servers in the database and updates
 * the {@link ConnectedServerManager} accordingly.
 * 
 * @author nathan
 * 
 */
public class LoadConnectedServersJob extends DatabaseJob {

	private final Map<String, String> f_passwords;

	/**
	 * 
	 * @param passwords
	 *            a map of all stored passwords, keyed by <code>user@host</code>
	 *            /
	 */
	public LoadConnectedServersJob(final Map<String, String> passwords) {
		super("Loading server location information");
		f_passwords = passwords;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		ConnectedServerManager.getInstance().updateConnectedServers(
				Data.getInstance().withReadOnly(
						ServerLocations.fetchQuery(f_passwords)));
		return Status.OK_STATUS;
	}

}
