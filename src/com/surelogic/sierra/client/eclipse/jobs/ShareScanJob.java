package com.surelogic.sierra.client.eclipse.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.eclipse.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.TimeseriesPromptFromJob;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.model.SierraServer;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.ScanVersionException;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.message.TimeseriesRequest;

public class ShareScanJob extends AbstractServerProjectJob {
	private final File f_scanFile;

	public ShareScanJob(ServerProjectGroupJob family, String projectName,
			SierraServer server, File scanFile, ServerFailureReport method) {
		super(family, "Sharing scan of project '" + projectName + "'", server,
				projectName, method);
		f_scanFile = scanFile;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(monitor);
		slMonitor.beginTask("Sharing scan of project " + f_projectName + " to "
				+ f_server.getLabel() + ".", 5);
		try {
			Scan scan = MessageWarehouse.getInstance().fetchScan(f_scanFile,
					true);
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} else {

				Set<String> timeseries = getTimeseriesOnTheServer(slMonitor);
				if (timeseries == null) {
					return Status.CANCEL_STATUS;
				}
				if (slMonitor.isCanceled()) {
					return null;
				} else {
					TimeseriesPromptFromJob prompt = new TimeseriesPromptFromJob(
							timeseries, f_projectName, f_server.getLabel());
					prompt.open();
					if (prompt.isCanceled()) {
						slMonitor.setCanceled(true);
						return Status.CANCEL_STATUS;
					} else {
						timeseries = prompt.getSelectedTimeseries();
					}
				}
				if (slMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				scan.getConfig().setTimeseries(
						new ArrayList<String>(timeseries));
				return publishRun(scan, slMonitor);
			}
		} catch (Exception e) {
			return createErrorStatus(50, e);
		}
	}

	private Set<String> getTimeseriesOnTheServer(SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			List<String> timeseries = SierraServiceClient.create(
					f_server.getServer())
					.getTimeseries(new TimeseriesRequest()).getTimeseries();
			if (timeseries == null) {
				timeseries = Collections.emptyList();
			}
			return new TreeSet<String>(timeseries);
		} catch (SierraServiceClientException e) {
			if (joinJob.troubleshoot(f_server)) {
				troubleshoot = getTroubleshootConnection(f_method, e);

				// We had a recoverable error. Rollback, run the appropriate
				// troubleshoot, and try again.
				troubleshoot.fix();
				if (troubleshoot.retry()) {
					return getTimeseriesOnTheServer(slMonitor);
				}
				joinJob.fail(f_server);
			}
			SLLogger.getLogger().log(Level.WARNING, I18N.err(89, f_server), e);
			return null;
		}

	}

	private IStatus publishRun(Scan scan, SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			SierraServiceClient.create(f_server.getServer()).publishRun(scan);
			f_server.markAsConnected();
			return Status.OK_STATUS;
		} catch (SierraServiceClientException e) {
			troubleshoot = getTroubleshootConnection(f_method, e);

			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			troubleshoot.fix();
			if (troubleshoot.retry()) {
				return publishRun(scan, slMonitor);
			} else {
				SLLogger.getLogger().log(Level.WARNING,
						I18N.err(87, f_projectName, f_server), e);
				return Status.CANCEL_STATUS;
			}
		} catch (ScanVersionException e) {
			final int errNo = 88;
			String scanVersion = scan.getVersion();
			if (scanVersion == null)
				scanVersion = "(none)";
			final String msg = I18N.err(errNo, scanVersion, f_projectName,
					f_server);
			// SLLogger.getLogger().log(Level.SEVERE, msg, e);
			IStatus s = SLStatus.createErrorStatus(errNo, msg);
			showErrorDialog(msg, e, "Error while publishing run", s);
			return s;
		}
	}
}
