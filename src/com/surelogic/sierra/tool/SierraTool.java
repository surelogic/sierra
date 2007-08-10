package com.surelogic.sierra.tool;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SierraTool extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.surelogic.sierra.tool";

	// The shared instance
	private static SierraTool plugin;

	/**
	 * The constructor
	 */
	public SierraTool() {
		if (plugin != null)
			throw new IllegalStateException(PLUGIN_ID + " class instance ("
					+ SierraTool.class.getName() + ") already exits");
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static SierraTool getDefault() {
		return plugin;
	}

}
