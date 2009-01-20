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

import com.surelogic.common.eclipse.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.eclipse.jobs.ShareScanRule;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.TimeseriesPromptFromJob;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Scan;
import com.surelogic.sierra.tool.message.ScanVersionException;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClient;
import com.surelogic.sierra.tool.message.SierraServiceClientException;
import com.surelogic.sierra.tool.message.TimeseriesRequest;

public class ShareScanJob extends AbstractServerProjectJob {

	private final File f_scanFile;

	public ShareScanJob(final ServerProjectGroupJob family,
			final String projectName, final ConnectedServer server,
			final File scanFile, final ServerFailureReport strategy) {
		super(family, "Sharing scan of project '" + projectName + "'", server,
				projectName, strategy);
		if (scanFile == null)
			throw new IllegalArgumentException(I18N.err(44, "scanFile"));
		f_scanFile = scanFile;
		setRule(ShareScanRule.getInstance());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final String msg = "Sharing scan of project " + f_projectName + " to "
				+ getServer().getName() + ".";
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, msg);
		slMonitor.begin(5);
		try {
			final Scan scan = MessageWarehouse.getInstance().fetchScan(
					f_scanFile, true);
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
					final TimeseriesPromptFromJob prompt = new TimeseriesPromptFromJob(
							timeseries, f_projectName, getServer().getName());
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
		} catch (final Exception e) {
			return createErrorStatus(50, e);
		}
	}

	private Set<String> getTimeseriesOnTheServer(
			final SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			List<String> timeseries = SierraServiceClient.create(
					getServer().getLocation()).getTimeseries(
					new TimeseriesRequest()).getTimeseries();
			if (timeseries == null) {
				timeseries = Collections.emptyList();
			}
			return new TreeSet<String>(timeseries);
		} catch (final SierraServiceClientException e) {
			if (joinJob != null && joinJob.troubleshoot(getServer())) {
				troubleshoot = getTroubleshootConnection(f_strategy, e);

				// We had a recoverable error. Rollback, run the appropriate
				// troubleshoot, and try again.
				ServerLocation fixed = troubleshoot.fix();
				if (troubleshoot.retry()) {
					/*
					 * First update our server information.
					 */
					changeAuthorization(fixed.getUser(), fixed.getPass(), fixed
							.isSavePassword());
					return getTimeseriesOnTheServer(slMonitor);
				}
				joinJob.fail(getServer());
			}
			SLLogger.getLogger().log(Level.WARNING, I18N.err(89, getServer()),
					e);
			return null;
		}

	}

	private IStatus publishRun(final Scan scan,
			final SLProgressMonitor slMonitor) {
		TroubleshootConnection troubleshoot;
		try {
			SierraServiceClient.create(getServer().getLocation()).publishRun(
					scan);
			ConnectedServerManager.getInstance().getStats(getServer())
					.markAsConnected();
			return Status.OK_STATUS;
		} catch (final SierraServiceClientException e) {
			troubleshoot = getTroubleshootConnection(f_strategy, e);

			// We had a recoverable error. Rollback, run the appropriate
			// troubleshoot, and try again.
			ServerLocation fixed = troubleshoot.fix();
			if (troubleshoot.retry()) {
				/*
				 * First update our server information.
				 */
				changeAuthorization(fixed.getUser(), fixed.getPass(), fixed
						.isSavePassword());
				return publishRun(scan, slMonitor);
			} else {
				SLLogger.getLogger().log(Level.WARNING,
						I18N.err(87, f_projectName, getServer()), e);
				return Status.CANCEL_STATUS;
			}
		} catch (final ScanVersionException e) {
			final int errNo = 88;
			String scanVersion = scan.getVersion();
			if (scanVersion == null) {
				scanVersion = "(none)";
			}
			final String msg = I18N.err(errNo, scanVersion, f_projectName,
					getServer());
			// SLLogger.getLogger().log(Level.SEVERE, msg, e);
			final IStatus s = SLEclipseStatusUtility.createErrorStatus(errNo,
					msg);
			showErrorDialog(msg, e, "Error while publishing run", s);
			return s;
		}
	}
}
