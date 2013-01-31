package com.surelogic.sierra.eclipse.teamserver;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.ui.DialogTouchNotificationUI;
import com.surelogic.sierra.eclipse.teamserver.preferences.LocalTeamServerPreferencesUtility;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The shared instance
	private static Activator f_plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		if (f_plugin != null)
			throw new IllegalStateException(Activator.class.getName()
					+ " instance already exits, it should be a singleton.");
		f_plugin = this;
	}

	@Override
  public void start(BundleContext context) throws Exception {
		super.start(context);
		f_plugin = this;

		/*
		 * "Touch" common-core-eclipse so the logging gets Eclipse-ified.
		 */
		SLEclipseStatusUtility.touch(new DialogTouchNotificationUI());
		
		LocalTeamServerPreferencesUtility.initializeDefaultScope();
	}

	@Override
  public void stop(BundleContext context) throws Exception {
		f_plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return f_plugin;
	}

	/**
	 * Gets the identifier for this plug in.
	 * 
	 * @return an identifier, such as <tt>com.surelogic.common</tt>. In rare
	 *         cases, for example bad plug in XML, it may be {@code null}.
	 * @see Bundle#getSymbolicName()
	 */
	public String getPlugInId() {
		return f_plugin.getBundle().getSymbolicName();
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(getDefault().getPlugInId(), path);
	}
}
