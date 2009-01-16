package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.swt.widgets.Shell;

import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.sierra.client.eclipse.dialogs.ServerAuthenticationDialog;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerStats;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.ServerLocation;

public abstract class ServerActionOnAProject {

	/**
	 * Performs the server action on the passed project.
	 * 
	 * @param projectName
	 *            a project name.
	 * @param server
	 *            a server.
	 * @param shell
	 *            a non-null shell.
	 */
	abstract public void run(final String projectName,
			final ConnectedServer server, final Shell shell);

	/**
	 * Utility routine to prompt for a password if required by the server. If
	 * the password was entered then the passed action is run.
	 * 
	 * @param projectName
	 *            the project to invoke the action on.
	 * @param server
	 *            the server to check if a password needs to be entered.
	 * @param shell
	 *            the shell.
	 * @param action
	 *            the action to invoke on the project and the server.
	 */
	public static void promptPasswordIfNecessary(final String projectName,
			ConnectedServer server, Shell shell,
			final ServerActionOnAProject action) {
		if (shell == null)
			shell = SWTUtility.getShell();

		final ConnectedServerManager mgr = ConnectedServerManager.getInstance();
		final ServerLocation location = server.getLocation();
		final ConnectedServerStats stats = mgr.getStats(server);

		if (!location.isSavePassword() && !stats.usedToConnectToAServer()) {
			ServerLocation fixed = ServerAuthenticationDialog.open(null,
					location);

			if (location == fixed) {
				/*
				 * Just stop, don't try to run the job...the dialog was
				 * canceled.
				 */
				return;
			}
			server = mgr.changeAuthorizationFor(server, fixed.getUser(), fixed
					.getPass(), fixed.isSavePassword());
			stats.setUsed();
		}
		action.run(projectName, server, shell);
	}
}
