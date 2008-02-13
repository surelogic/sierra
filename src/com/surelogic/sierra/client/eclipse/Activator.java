package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.surelogic.common.eclipse.dialogs.ExceptionDetailsDialog;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.MarkersHandler;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;

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
		if (f_plugin != null)
			throw new IllegalStateException(I18N.err(1, PLUGIN_ID,
					Activator.class.getName()));
		f_plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		/*
		 * "Touch" common-eclipse so the logging gets Eclipse-ified.
		 */
		SLStatus.touch();

		try {
			// startup the database and ensure its schema is up to date
			System.setProperty("derby.storage.pageCacheSize", "2500");
			System.setProperty("derby.stream.error.file", getDerbyLogFile());
			Data.bootAndCheckSchema();
			// load up persisted sierra servers
			SierraServerManager.getInstance().load(getServerSaveFile());
			// load up persisted sierra selections
			SelectionManager.getInstance().load(getSelectionSaveFile());
			// start observing data changes
			Projects.getInstance().refresh();
			// listen changes to the active editor and preference listener
			MarkersHandler handler = MarkersHandler.getInstance();
			handler.addMarkerListener();
			getDefault().getPluginPreferences().addPropertyChangeListener(
					handler);
		} catch (FutureDatabaseException e) {
			/*
			 * The database schema version is too new, we need to delete it to
			 * run with this version of the code.
			 * 
			 * This could occur if the user reverted to a previously installed
			 * version of Sierra. (RFR requirement 3.1.15)
			 */
			f_databaseInSync.set(false);
			PreferenceConstants.setDeleteDatabaseOnStartup(true);
			final String msg = I18N.err(37, e.getSchemaVersion(), e
					.getCodeVersion());
			SLLogger.getLogger().log(Level.WARNING, msg, e);
			final ExceptionDetailsDialog report = new ExceptionDetailsDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), "No Such Query", PlatformUI
							.getWorkbench().getDisplay().getSystemImage(
									SWT.ICON_WARNING), msg, null, Activator
							.getDefault());
			report.open();
			final UIJob restartEclipseJob = new SLUIJob() {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					PlatformUI.getWorkbench().restart();
					return Status.OK_STATUS;
				}
			};
			restartEclipseJob.schedule();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			if (f_databaseInSync.get()) {
				SierraServerManager.getInstance().save(getServerSaveFile());
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

	private File getServerSaveFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString() + File.separator
				+ "servers.xml");
	}

	private File getSelectionSaveFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString() + File.separator
				+ "selections.xml");
	}

	public File getFindingDetailsViewSaveFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString() + File.separator
				+ "finding-details-view.xml");
	}

	private String getDerbyLogFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return pluginState.toOSString() + File.separator + "derby.log";
	}

	public String getDirectoryOf(final String plugInId) {
		final Bundle bundle = Platform.getBundle(plugInId);
		if (bundle == null) {
			throw new IllegalStateException("null bundle returned for "
					+ plugInId);
		}
		final URL relativeURL = bundle.getEntry("");
		try {
			URL commonPathURL = FileLocator.resolve(relativeURL);
			final String commonDirectory = commonPathURL.getPath();
			if (commonDirectory.startsWith("file:")
					&& commonDirectory.endsWith(".jar!/")) {
				// Jar file
				return commonDirectory.substring(5,
						commonDirectory.length() - 2);
			}
			return commonDirectory;
		} catch (Exception e) {
			throw new IllegalStateException(
					"failed to resolve a path for the URL " + relativeURL);
		}
	}
}
