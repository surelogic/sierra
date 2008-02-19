package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.tool.message.GlobalSettings;
import com.surelogic.sierra.tool.message.GlobalSettingsRequest;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public final class GetGlobalResultFiltersJob extends DatabaseJob {

	private final SierraServer f_server;

	public GetGlobalResultFiltersJob(SierraServer server) {
		super("Getting scan filter settings from " + server.getLabel());
		f_server = server;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		final String msg = "Getting scan filter settings from the Sierra team server '"
				+ f_server + "'";
		slMonitor.beginTask(msg, 5);
		IStatus status = null;
		try {
			final Connection conn = Data.transactionConnection();
			try {
				status = getResultFilters(conn, slMonitor);
			} catch (Throwable e) {
				final int errNo = 48;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLStatus.createWarningStatus(errNo, errMsg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				final int errNo = 48;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLStatus.createWarningStatus(errNo, errMsg, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus getResultFilters(Connection conn,
			SLProgressMonitor slMonitor) throws SQLException {
		try {
			final SierraService service = SierraServiceClient.create(f_server
					.getServer());
			final GlobalSettings settings = service
					.getGlobalSettings(new GlobalSettingsRequest());
			SettingsManager.getInstance(conn).writeGlobalSettings(
					settings.getFilter());
			conn.commit();
		} catch (SierraServiceClientException e) {
			TroubleshootConnection troubleshoot;
			if (e instanceof InvalidLoginException) {
				troubleshoot = new TroubleshootWrongAuthentication(f_server,
						null);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_server, null);
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return getResultFilters(conn, slMonitor);
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
