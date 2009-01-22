package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public final class SendScanFiltersJob extends DatabaseJob {
	public static final boolean ENABLED = true;
	private final ServerFailureReport f_strategy;
	private ConnectedServer f_server;
	private final String f_name;

	public SendScanFiltersJob(final ServerFailureReport strategy,
			final ConnectedServer server, final String name) {
		super("Sending local scan filter settings to " + server.getName());
		f_server = server;
		f_name = name;
		f_strategy = strategy;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String msg = "Sending local scan filter settings to the Sierra team server '"
				+ f_server + "'";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin(5);
		IStatus status = null;
		try {
			final Connection conn = Data.getInstance().readOnlyConnection();
			try {
				status = sendResultFilters(conn, slMonitor);

				if (status.getSeverity() == IStatus.OK) {	 			
					final Job sync = new SynchronizeJob(null, null, f_server, 
							                            ServerSyncType.BUGLINK, true, f_strategy);
					sync.schedule();
				}
			} catch (final Throwable e) {
				final int errNo = 49;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLEclipseStatusUtility.createWarningStatus(errNo,
						errMsg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (final SQLException e1) {
			if (status == null) {
				final int errNo = 49;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLEclipseStatusUtility.createWarningStatus(errNo,
						errMsg, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus sendResultFilters(final Connection conn,
			final SLProgressMonitor slMonitor) throws SQLException {
		try {
			Data.getInstance().withReadOnly(
					SettingQueries.copyDefaultScanFilterToServer(f_server
							.getLocation(), f_name));
			ConnectedServerManager.getInstance().getStats(f_server)
					.markAsConnected();
		} catch (final SierraServiceClientException e) {
			TroubleshootConnection troubleshoot;
			if (e instanceof InvalidLoginException) {
				troubleshoot = new TroubleshootWrongAuthentication(f_strategy,
						f_server.getLocation());
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_strategy, f_server
						.getLocation());
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			final ServerLocation fixed = troubleshoot.fix();
			if (troubleshoot.retry()) {
				f_server = ConnectedServerManager.getInstance()
						.changeAuthorizationFor(f_server, fixed.getUser(),
								fixed.getPass(), fixed.isSavePassword());
				return sendResultFilters(conn, slMonitor);
			} else {
				final int errNo = 49;
				final String msg = I18N.err(errNo, f_server);
				SLLogger.getLogger().log(Level.WARNING, msg, e);
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}
}
