package com.surelogic.sierra.gwt.client;

/**
 * Listens for changes in the web client context (a.k.a. browser url).
 * 
 */
public interface ContextListener {

	/**
	 * Called when the web client context has changed.
	 * 
	 * @param context
	 *            the new context
	 */
	void onChange(Context context);

}
