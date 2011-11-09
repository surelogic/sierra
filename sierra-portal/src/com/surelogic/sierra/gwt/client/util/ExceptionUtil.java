package com.surelogic.sierra.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.LogPane;
import com.surelogic.sierra.gwt.client.SessionManager;

public final class ExceptionUtil {

	private ExceptionUtil() {
		// no instance
	}

	public static void log(final Throwable caught) {
		GWT.log("ExceptionTracker", caught);
		LogPane.getInstance().log(caught);
	}

	/**
	 * This method is used as the standard way to log an exception, and then
	 * terminate the session.
	 * 
	 * @param caught
	 */
	public static void handle(final Throwable caught) {
		log(caught);
		SessionManager
				.logout("Unable to communicate with server. (Server may be down)");
	}
}
