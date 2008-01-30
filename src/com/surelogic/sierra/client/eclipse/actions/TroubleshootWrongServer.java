package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.Activator;
import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootWrongServer extends TroubleshootConnection {

	public TroubleshootWrongServer(SierraServer server, String projectName) {
		super(server, projectName);
	}

	@Override
	public void fix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				final String sl = f_server.getLabel();
				final String msg = I18N.err(24, sl, f_projectName, sl,
						getServer().getLabel(), f_projectName, sl, getServer()
								.getLabel());
				final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(),
						"Connected to the Wrong Sierra Team Server", null, msg,
						null, Activator.getDefault());
				report.open();
				SLLogger.getLogger().warning(msg);
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
