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
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.tool.message.GlobalSettings;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.SierraService;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public final class SendGlobalResultFiltersJob extends DatabaseJob {
  private final ServerFailureReport f_method;
	private final SierraServer f_server;

	public SendGlobalResultFiltersJob(ServerFailureReport method, SierraServer server) {
		super("Sending scan filter settings to " + server.getLabel());
		f_server = server;
		f_method = method;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		final String msg = "Sending scan filter settings to the Sierra team server '"
				+ f_server + "'";
		slMonitor.beginTask(msg, 5);
		IStatus status = null;
		try {
			final Connection conn = Data.readOnlyConnection();
			try {
				status = sendResultFilters(conn, slMonitor);
			} catch (Throwable e) {
				final int errNo = 49;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLStatus.createWarningStatus(errNo, errMsg, e);
				conn.rollback();
			} finally {
				conn.close();
			}
		} catch (SQLException e1) {
			if (status == null) {
				final int errNo = 49;
				final String errMsg = I18N.err(errNo, f_server);
				status = SLStatus.createWarningStatus(errNo, errMsg, e1);
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
			f_server.markAsConnected();
		} catch (SierraServiceClientException e) {
			TroubleshootConnection troubleshoot;
			if (e instanceof InvalidLoginException) {
				troubleshoot = 
					new TroubleshootWrongAuthentication(f_method, f_server, null);
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_method, f_server, null);
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
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
