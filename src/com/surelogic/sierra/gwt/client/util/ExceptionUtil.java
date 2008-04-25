package com.surelogic.sierra.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.ClientContext;
import com.surelogic.sierra.gwt.client.LogPanel;

/**
 * TODO Look up standard GWT exception model, this is an unverified approach
 * 
 */
public class ExceptionUtil {

	private ExceptionUtil() {
		// no instance
	}

	public static void log(Throwable caught) {
		GWT.log("ExceptionTracker", caught);
		LogPanel.getInstance().log(caught);
	}

	/**
	 * This method is used as the standard way to log an exception, and then
	 * terminate the session.
	 * 
	 * @param caught
	 */
	public static void handle(Throwable caught) {
		log(caught);
		ClientContext
				.logout("Unable to communicate with server. (Server may be down)");
	}
}
