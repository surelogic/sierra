package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public final class OverwriteLocalScanFilterJob extends DatabaseJob {
	private static final int ERROR_NUM = 48; // FIX
	private final ServerFailureReport f_method;
	private final SierraServer f_server;
	private final ScanFilter f_filter;
	
	public OverwriteLocalScanFilterJob(ServerFailureReport method, SierraServer server, ScanFilter f) {
		super("Overwriting local scan filter with filter '"+f.getName()+"' from " + server.getLabel());
		f_server = server;
		f_filter = f;
		f_method = method;		
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final String msg = "Overwriting local scan filter with filter '"+f_filter.getName()+
		                   "' from the Sierra team server '" + f_server + "'";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin(5);
		IStatus status = null;
		try {
			status = overwriteFilter(slMonitor);
		} catch (final Throwable e) {
			final int errNo = ERROR_NUM; 
			final String errMsg = I18N.err(errNo, f_server);
			status = SLEclipseStatusUtility.createWarningStatus(errNo, errMsg,
					e);
		}
		return status;
	}

	private IStatus overwriteFilter(SLProgressMonitor slMonitor)
			throws SQLException {
		try {
			final DBQuery<?> query = SettingQueries.updateDefaultScanFilter(f_filter.getUid());			
			Data.getInstance().withTransaction(query);
			f_server.markAsConnected();
		} catch (final SierraServiceClientException e) {
			TroubleshootConnection troubleshoot;
			if (e instanceof InvalidLoginException) {
				troubleshoot = new TroubleshootWrongAuthentication(f_method,
						f_server, null);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_method, f_server,
						null);
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return overwriteFilter(slMonitor);
			} else {
				final int errNo = ERROR_NUM;
				final String msg = I18N.err(errNo, f_server);
				SLLogger.getLogger().log(Level.WARNING, msg, e);
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}
}
