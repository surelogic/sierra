package com.surelogic.sierra.client.eclipse;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.surelogic.sierra.client.eclipse";

	public static final String XML_ENCODING = "UTF-8";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		if (plugin != null)
			throw new IllegalStateException(PLUGIN_ID + " class instance ("
					+ Activator.class.getName() + ") already exits");
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// startup the database and ensure its schema is up to date
		Data.bootAndCheckSchema();
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
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is given focus.
	 * <P>
	 * This method must be called from a UI thread or it will throw a
	 * {@link NullPointerException}. *
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @return the shown view or <code>null</code>.
	 */
	public static IViewPart showView(final String viewId) {
		try {
			final IViewPart view = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(viewId);
			return view;
		} catch (PartInitException e) {
			SLog.logError("Unable to open the view identified by " + viewId
					+ ".", e);
		}
		return null;
	}

	public static IViewPart showView(final String viewId,
			final String secondaryId, final int mode) {
		try {
			final IViewPart view = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().showView(
							viewId, secondaryId, mode);
			return view;
		} catch (PartInitException e) {
			SLog.logError("Unable to open the view identified by " + viewId
					+ " " + secondaryId + ".", e);
		}
		return null;
	}
}
