package com.surelogic.sierra.client.eclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.surelogic.sierra.client.eclipse.model.Project;

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
		// startup the database and ensure its schema is up to date
		Data.bootAndCheckSchema();
		// start observing data changes
		Project.getInstance().refresh();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		f_plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return f_plugin;
	}
}
