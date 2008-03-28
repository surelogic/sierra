package com.surelogic.sierra.client.eclipse.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootNoSuchServer extends TroubleshootConnection {

	public TroubleshootNoSuchServer(SierraServer server, String projectName) {
		super(server, projectName);
	}

	@Override
	protected void realFix() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				final String sl = f_server.getLabel();
				final int errNo = 23;
				final String msg = I18N.err(errNo, sl, sl, sl, sl, sl, sl);
				final IStatus reason = SLStatus.createWarningStatus(errNo, msg);
				ErrorDialogUtility.open(null,
						"Sierra Team Server Connection Failed", reason);
			}
		});
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
}
