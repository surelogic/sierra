package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import com.surelogic.common.eclipse.dialogs.ErrorDialogUtility;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.sierra.client.eclipse.actions.MarkersHandler;
import com.surelogic.sierra.client.eclipse.jobs.DeleteUnfinishedScans;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.views.SierraServersAutoSync;
import com.surelogic.sierra.client.eclipse.views.adhoc.AdHocDataSource;

/**
 * The activator class controls the plug-in life cycle
 */
public final class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.surelogic.sierra.client.eclipse";

	public static final String XML_ENCODING = "UTF-8";

	// The shared instance
	private static Activator f_plugin;

	/**
	 * Indicates that the schema versions in the code and in the database are in
	 * sync.
	 */
	private static final AtomicBoolean f_databaseInSync = new AtomicBoolean(
			true);

	/**
	 * The constructor
	 */
	public Activator() {
		if (f_plugin != null) {
			throw new IllegalStateException(I18N.err(1, PLUGIN_ID,
					Activator.class.getName()));
		}
		f_plugin = this;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		/*
		 * "Touch" common-eclipse so the logging gets Eclipse-ified.
		 */
		SLEclipseStatusUtility.touch();

		try {
			// startup the database and ensure its schema is up to date
			System.setProperty("derby.stream.error.file", getDerbyLogFile());
			// load up persisted sierra servers
			ConnectedServerManager.getInstance().init();
			// load up persisted sierra selections
			SelectionManager.getInstance().load(getSelectionSaveFile());
			// start observing data changes
			Projects.getInstance().init();
			BuglinkData.getInstance().init();
			// listen changes to the active editor and preference listener
			final MarkersHandler handler = MarkersHandler.getInstance();
			handler.addMarkerListener();
			getDefault().getPluginPreferences().addPropertyChangeListener(
					handler);

			SierraServersAutoSync.start();
			new DeleteUnfinishedScans().schedule();
		} catch (final FutureDatabaseException e) {
			/*
			 * The database schema version is too new, we need to delete it to
			 * run with this version of the code.
			 * 
			 * This could occur if the user reverted to a previously installed
			 * version of Sierra. (RFR requirement 3.1.15)
			 */
			f_databaseInSync.set(false);
			PreferenceConstants.setDeleteDatabaseOnStartup(true);
			final int errNo = 37;
			final String msg = I18N.err(errNo, e.getSchemaVersion(), e
					.getCodeVersion());
			final IStatus reason = SLEclipseStatusUtility.createWarningStatus(
					errNo, msg, e);
			ErrorDialogUtility.open(null, null, reason);
			final UIJob restartEclipseJob = new SLUIJob() {
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					PlatformUI.getWorkbench().restart();
					return Status.OK_STATUS;
				}
			};
			restartEclipseJob.schedule();
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			SierraServersAutoSync.stop();
			AdHocDataSource.getInstance().dispose();
			Projects.getInstance().dispose();
			BuglinkData.getInstance().dispose();
			if (f_databaseInSync.get()) {
				ConnectedServerManager.getInstance().dispose();
				SelectionManager.getInstance().save(getSelectionSaveFile());
			}
			f_plugin = null;
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return f_plugin;
	}

	private File getSelectionSaveFile() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString() + File.separator
				+ "selections.xml");
	}

	public File getFindingDetailsViewSaveFile() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString() + File.separator
				+ "finding-details-view.xml");
	}

	private String getDerbyLogFile() {
		final IPath pluginState = Activator.getDefault().getStateLocation();
		return pluginState.toOSString() + File.separator + "derby.log";
	}
}
