package com.surelogic.sierra.client.eclipse.actions;

import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.BalloonUtility;
import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.model.SierraServer;

public final class TroubleshootException extends TroubleshootConnection {
	private final boolean f_isSevere;
	private Exception f_ex;

	public TroubleshootException(SierraServer server, String projectName, 
								 Exception e, boolean severe) {
		super(server, projectName);
		f_ex = e;
		f_isSevere = severe;
	}

	@Override
	protected void realFix() {
		final String sl = f_server.getLabel();
		final int errNo = 97;
		final String msg = I18N.err(errNo, f_projectName, sl);
		
		if (f_isSevere) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					final IStatus reason = SLStatus.createWarningStatus(errNo, msg);
					ErrorDialogUtility.open(null,
							"Problem Getting Server Info", reason, false);
				}
			});
		} else {
			BalloonUtility.showMessage(msg, 
	                   "Caught an "+f_ex.getClass().getName()+
	                   " ("+f_ex.getMessage()+"). See the log for details.");			
		}
		SLLogger.log(f_isSevere ? Level.SEVERE : Level.WARNING, msg, f_ex);
		
		/*
		 * We just want the job to fail.
		 */
		setRetry(false);
	}
	
	@Override
	public boolean failServer() {
		return false;
	}
}
