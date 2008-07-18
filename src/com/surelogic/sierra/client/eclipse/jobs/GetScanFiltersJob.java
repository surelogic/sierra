package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public class GetScanFiltersJob extends DatabaseJob {
	private final ServerFailureReport f_method;
	private final SierraServer f_server;

	public GetScanFiltersJob(ServerFailureReport method, SierraServer server) {
		super("Getting scan filter settings from " + server.getLabel());
		f_server = server;
		f_method = method;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor);
		final String msg = "Getting scan filter settings from the Sierra team server '"
				+ f_server + "'";
		slMonitor.beginTask(msg, 5);
		IStatus status = null;
		try {
			status = getResultFilters(slMonitor);
		} catch (final Throwable e) {
			final int errNo = 48;
			final String errMsg = I18N.err(errNo, f_server);
			status = SLEclipseStatusUtility.createWarningStatus(errNo, errMsg, e);
		}
		return status;
	}

	private IStatus getResultFilters(SLProgressMonitor slMonitor)
			throws SQLException {
		try {
			final DBQuery<?> query = SettingQueries.retrieveScanFilters(
					f_server.getServer(), Data.getInstance().withReadOnly(SettingQueries
							.scanFilterRequest()));
			f_server.markAsConnected();
			Data.getInstance().withTransaction(query);
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
				return getResultFilters(slMonitor);
			} else {
				final int errNo = 48;
				final String msg = I18N.err(errNo, f_server);
				SLLogger.getLogger().log(Level.WARNING, msg, e);
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}
}
