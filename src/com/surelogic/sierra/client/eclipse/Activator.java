package com.surelogic.sierra.client.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.actions.MarkersHandler;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.SierraServerManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

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

		final Runtime rt = Runtime.getRuntime();
		SLLogger.getLogger().info(
				"Sierra loaded : Java runtime: maxMemory="
						+ (rt.maxMemory() / 1024 / 1024) + " MB; totalMemory="
						+ (rt.totalMemory() / 1024 / 1024) + " MB; freeMemory="
						+ (rt.freeMemory() / 1024 / 1024)
						+ " MB; availableProcessors="
						+ rt.availableProcessors());

		// TODO find a better place to define this system property
		System.setProperty("derby.storage.pageCacheSize", "2500");
		// startup the database and ensure its schema is up to date
		Data.bootAndCheckSchema();
		// load up persisted sierra servers
		SierraServerManager.getInstance().load(getServerSaveFile());
		// start observing data changes
		Projects.getInstance().refresh();
		// listen changes to the active editor
		MarkersHandler.getInstance().addMarkerListener();
		// add preference listener
		getDefault().getPluginPreferences().addPropertyChangeListener(
				MarkersHandler.getInstance());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			SierraServerManager.getInstance().save(getServerSaveFile());
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
}
