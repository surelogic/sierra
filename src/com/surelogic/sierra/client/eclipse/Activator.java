package com.surelogic.sierra.client.eclipse;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.surelogic.common.XUtil;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.sierra.client.eclipse.actions.MarkersHandler;
import com.surelogic.sierra.client.eclipse.jetty.EmbeddedJettyUtility;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;

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
	 * The constructor
	 */
	public Activator() {
		if (f_plugin != null)
			throw new IllegalStateException(PLUGIN_ID + " class instance ("
					+ Activator.class.getName() + ") already exits");
		f_plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		/*
		 * "Touch" common-eclipse so the logging gets Eclipse-ified.
		 */
		SLStatus.touch();

		// TODO find a better place to define this system property
		System.setProperty("derby.storage.pageCacheSize", "2500");
		// startup the database and ensure its schema is up to date
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
		getDefault().getPluginPreferences().addPropertyChangeListener(handler);
		if (XUtil.useExperimental()) {
			// embedded Jetty server
			EmbeddedJettyUtility.startup();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			SierraServerManager.getInstance().save(getServerSaveFile());
			SelectionManager.getInstance().save(getSelectionSaveFile());
			EmbeddedJettyUtility.shutdown();
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
		return new File(pluginState.toOSString()
				+ System.getProperty("file.separator") + "servers.xml");
	}

	private File getSelectionSaveFile() {
		IPath pluginState = Activator.getDefault().getStateLocation();
		return new File(pluginState.toOSString()
				+ System.getProperty("file.separator") + "selections.xml");
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
			if (commonDirectory.startsWith("file:") && 
			    commonDirectory.endsWith(".jar!/")) {
			  // Jar file
			  return commonDirectory.substring(5, commonDirectory.length()-2);
			}
			return commonDirectory;
		} catch (Exception e) {
			throw new IllegalStateException(
					"failed to resolve a path for the URL " + relativeURL);
		}
	}
}
