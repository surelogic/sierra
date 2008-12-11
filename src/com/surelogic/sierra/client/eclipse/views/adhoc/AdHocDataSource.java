package com.surelogic.sierra.client.eclipse.views.adhoc;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.adhoc.AdHocManager;
import com.surelogic.common.adhoc.AdHocManagerAdapter;
import com.surelogic.common.adhoc.AdHocQueryResult;
import com.surelogic.common.adhoc.IAdHocDataSource;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.DBConnection;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Activator;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

public final class AdHocDataSource extends AdHocManagerAdapter implements
		IAdHocDataSource {

	private static final AdHocDataSource INSTANCE = new AdHocDataSource();

	private AdHocDataSource() {
		// Do nothing
	}

	static {
		INSTANCE.init();
	}

	public static AdHocDataSource getInstance() {
		return INSTANCE;
	}

	public void init() {
		getManager().addObserver(this);
		getManager().setGlobalVariableValue(AdHocManager.DATABASE,
				"Sierra Client Database");
	}

	public void dispose() {
		getManager().removeObserver(this);
		AdHocManager.shutdown();
	}

	public DBConnection getDB() {
		return Data.getInstance();
	}

	public int getMaxRowsPerQuery() {
		return PreferenceConstants.getFindingsListLimit();
	}

	public File getQuerySaveFile() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString()
				+ System.getProperty("file.separator") + "queries.xml");
	}

	public void badQuerySaveFileNotification(final Exception e) {
		SLLogger.getLogger().log(Level.SEVERE,
				I18N.err(4, getQuerySaveFile().getAbsolutePath()), e);
	}

	public URL getDefaultQueryUrl() {
		return null;
	}

	public static AdHocManager getManager() {
		return AdHocManager.getInstance(INSTANCE);
	}

	@Override
	public void notifySelectedResultChange(final AdHocQueryResult result) {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				final IViewPart view = ViewUtility.showView(
						QueryResultsView.class.getName(), null,
						IWorkbenchPage.VIEW_VISIBLE);
				if (view instanceof QueryResultsView) {
					final QueryResultsView queryResultsView = (QueryResultsView) view;
					queryResultsView.displayResult(result);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
