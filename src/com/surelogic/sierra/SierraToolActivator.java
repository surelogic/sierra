package com.surelogic.sierra;

import java.net.URL;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SierraToolActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.surelogic.sierra.tool";

	private static final String PMD_RULES_ALL = "/Tools/pmd-3.9/all.xml";

	private static final String FB_HOME = "/Tools/FB/lib/findbugs.jar";

	private static final String FB_HOME_DIR = "/Tools/FB";

	// The shared instance
	private static SierraToolActivator plugin;

	/**
	 * The constructor
	 */
	public SierraToolActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SierraToolActivator getDefault() {
		return plugin;
	}

	public static URL getPMDRulesAll() {
		final URL result = SierraToolActivator.class.getResource(PMD_RULES_ALL);
		return result;
	}

	public static URL getFBHome() {
		final URL result = SierraToolActivator.class.getResource(FB_HOME);
		return result;
	}

	public static URL getFBHomeDir() {
		final URL result = SierraToolActivator.class.getResource(FB_HOME_DIR);
		return result;
	}

}
