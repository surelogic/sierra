package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.tool.message.GlobalSettings;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public final class SendGlobalResultFiltersJob extends DatabaseJob {

	private final SierraServer f_server;

	public SendGlobalResultFiltersJob(SierraServer server) {
		super("Sending local settings to " + server.getLabel());
		f_server = server;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		final String msg = "Sending local settings to Sierra server '"
				+ f_server + "'";
		slMonitor.beginTask(msg, 5);
		IStatus status = null;
		try {
			final Connection conn = Data.readOnlyConnection();
			try {
				status = sendResultFilters(conn, slMonitor);
			} catch (Throwable e) {
				SLLogger.getLogger().log(Level.SEVERE, msg + " failed.", e);
				status = SLStatus.createErrorStatus(msg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				SLLogger.getLogger().log(Level.SEVERE, msg + " failed.", e1);
				status = SLStatus.createErrorStatus(msg, e1);
			}
		}
		if (status == null) {
			status = Status.OK_STATUS;
		}
		return status;
	}

	private IStatus sendResultFilters(Connection conn,
			SLProgressMonitor slMonitor) throws SQLException {
		try {
			final GlobalSettings settings = new GlobalSettings();
			settings.getFilter().addAll(
					SettingsManager.getInstance(conn).getGlobalSettings());
			final SierraService service = SierraServiceClient.create(f_server
					.getServer());
			service.writeGlobalSettings(settings);
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
				return sendResultFilters(conn, slMonitor);
			} else {
				SLLogger.getLogger().log(Level.WARNING,
						"Failed to write settings to " + f_server, e);
				return SLStatus.createErrorStatus(
						"Failed to write settings to " + f_server, e);
			}
		}
		return Status.OK_STATUS;
	}
}
