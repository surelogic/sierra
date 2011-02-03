package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootNoSuchServer;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongAuthentication;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ExtensionName;
import com.surelogic.sierra.tool.message.InvalidLoginException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

public class GetScanFiltersJob extends AbstractSierraDatabaseJob {
	private final ServerFailureReport f_strategy;
	private ConnectedServer f_server;

	public GetScanFiltersJob(final ServerFailureReport strategy,
			final ConnectedServer server) {
		super("Getting scan filter settings from " + server.getName());
		f_server = server;
		f_strategy = strategy;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String msg = "Getting scan filter settings from the Sierra team server '"
				+ f_server + "'";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin(5);
		IStatus status = null;
		try {
			status = getResultFilters(slMonitor);
		} catch (final Throwable e) {
			final int errNo = 48;
			final String errMsg = I18N.err(errNo, f_server);
			status = SLEclipseStatusUtility.createWarningStatus(errNo, errMsg,
					e);
		}
		return status;
	}

	private IStatus getResultFilters(final SLProgressMonitor slMonitor)
			throws SQLException {
		try {
			final List<ExtensionName> localExtensions = Data.getInstance()
					.withReadOnly(SettingQueries.localExtensions());
			final DBQuery<?> query = SettingQueries.retrieveScanFilters(
					f_server.getLocation(), Data.getInstance().withReadOnly(
							SettingQueries.scanFilterRequest()),
					localExtensions);
			ConnectedServerManager.getInstance().getStats(f_server)
					.markAsConnected();
			Data.getInstance().withTransaction(query);
		} catch (final SierraServiceClientException e) {
			TroubleshootConnection troubleshoot;
			if (e instanceof InvalidLoginException) {
				troubleshoot = new TroubleshootWrongAuthentication(f_strategy,
						f_server.getLocation());
			} else {
				troubleshoot = new TroubleshootNoSuchServer(f_strategy,
						f_server.getLocation());
			}
			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			final ServerLocation fixed = troubleshoot.fix();
			if (troubleshoot.retry()) {
				f_server = ConnectedServerManager.getInstance()
						.changeAuthorizationFor(f_server, fixed.getUser(),
								fixed.getPass(), fixed.isSavePassword());
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
