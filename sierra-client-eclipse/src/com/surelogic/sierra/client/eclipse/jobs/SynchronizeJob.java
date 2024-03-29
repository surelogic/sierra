package com.surelogic.sierra.client.eclipse.jobs;

import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.jobs.KeywordAccessRule;
import com.surelogic.common.core.jobs.SLProgressMonitorWrapper;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.DBTransaction;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootConnection;
import com.surelogic.sierra.client.eclipse.actions.TroubleshootWrongServer;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;
import com.surelogic.sierra.jdbc.settings.ServerScanFilterInfo;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.tool.message.ExtensionName;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.ServerMismatchException;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * This job explicitly adds the {@link DatabaseAccessRule} to run like a
 * database job.
 */
public class SynchronizeJob extends AbstractServerProjectJob {
	private final boolean f_force;

	/**
	 * Constructs a job.
	 * 
	 * @param family
	 *            the job family, may be {@code null}.
	 * @param server
	 *            the non-null server to contact.
	 * @param syncType
	 *            the non-null {@link ServerSyncType}.
	 * @param force
	 *            when {@code true} it causes the synchronize to be attempted
	 *            even if too many failures have occurred in the past.
	 * @param strategy
	 *            the method to report problems that the job encounters.
	 */
	public SynchronizeJob(final ServerProjectGroupJob family,
			final ConnectedServer server, final boolean force,
			final ServerFailureReport strategy) {
		super(family, "Synchronizing BugLink data for server '"
				+ server.getName() + "'", server, null, strategy);
		f_force = force;
		setRule(KeywordAccessRule.getInstance(JobConstants.ACCESS_KEY));
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final int retryThreshold = EclipseUtility
				.getIntPreference(SierraPreferencesUtility.SERVER_INTERACTION_RETRY_THRESHOLD);
		final int numProblems = ConnectedServerManager.getInstance()
				.getStats(getServer()).getProblemCount()
				+ Projects.getInstance().getConsecutiveConnectFailuresFor(f_projectName);
		if (!f_force && numProblems > retryThreshold) {
			return Status.CANCEL_STATUS;
		}
		final SLProgressMonitor slMonitor = new SLProgressMonitorWrapper(
				monitor, getName());
		slMonitor.begin(6);
		try {
			return synchronize(new SyncTransaction(slMonitor), slMonitor);
		} catch (final Throwable e) {
			return createWarningStatus(51, e);
		}
	}

	private IStatus synchronize(final DBTransaction<Boolean> sync,
			final SLProgressMonitor slMonitor) {
		try {
			final boolean updated = Data.getInstance().withTransaction(sync);
			if (slMonitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			} else {
				if (updated) {
					DatabaseHub.getInstance().notifyServerSynchronized();
				}
				return Status.OK_STATUS;
			}
		} catch (final TransactionException e) {
			final Throwable cause = e.getCause();
			TroubleshootConnection troubleshoot = null;
			if (joinJob == null || joinJob.troubleshoot(getServer())) {
				if (cause instanceof ServerMismatchException) {
					troubleshoot = new TroubleshootWrongServer(f_strategy,
							getServer().getLocation(), f_projectName);
				} else if (cause instanceof SierraServiceClientException) {
					troubleshoot = getTroubleshootConnection(f_strategy,
							(SierraServiceClientException) cause);
				}
				if (troubleshoot != null && troubleshoot.retry()) {
					final ServerLocation fixed = troubleshoot.fix();
					if (troubleshoot.retry()) {
						/*
						 * First update our server information.
						 */
						changeAuthorization(fixed.getUser(), fixed.getPass(),
								fixed.isSavePassword());
						return synchronize(sync, slMonitor);
					}
				}
			}
			if (joinJob != null) {
				joinJob.fail(getServer());
			}
			return fail(e);
		}
	}

	private class SyncTransaction implements DBTransaction<Boolean> {
		private final SLProgressMonitor slMonitor;

		SyncTransaction(final SLProgressMonitor slMonitor) {
			this.slMonitor = slMonitor;
		}

		@Override
    public Boolean perform(final Connection conn) throws Exception {
			final Query q = new ConnectionQuery(conn);
			SettingQueries.updateServerInfo(getServer().getLocation()).perform(
					q);
			boolean updated = false;
			if (joinJob == null || joinJob.process(getServer())) {
				final ServerLocation loc = getServer().getLocation();
				final List<ExtensionName> localExtensions = SettingQueries
						.localExtensions().perform(q);
				final ListCategoryResponse categories = SettingQueries
						.retrieveCategories(loc,
								SettingQueries.categoryRequest().perform(q),
								localExtensions).perform(q);
				final ServerScanFilterInfo filters = SettingQueries
						.retrieveScanFilters(loc,
								SettingQueries.scanFilterRequest().perform(q),
								localExtensions).perform(q);
				updated = !filters.getDeletions().isEmpty()
						|| !filters.getScanFilters().isEmpty()
						|| !categories.getDeletions().isEmpty()
						|| !categories.getFilterSets().isEmpty();
				slMonitor.worked(1);
			}
			ConnectedServerManager.getInstance().getStats(getServer())
					.markAsConnected();
			if (slMonitor.isCanceled()) {
				conn.rollback();
				return false;
			} else {
				return updated;
			}
		}
	}

	private IStatus fail(final Exception e) {
		final String msg = I18N.err(51, f_projectName, getServer());
		SLLogger.getLogger().log(Level.WARNING, msg, e);
		return Status.CANCEL_STATUS;
	}
}
