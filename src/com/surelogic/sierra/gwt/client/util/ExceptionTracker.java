package com.surelogic.sierra.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.LogPanel;

/**
 * TODO Look up standard GWT exception model, this is an unverified approach
 * 
 */
public class ExceptionTracker {

	private ExceptionTracker() {
		// no instance
	}

	public static void logException(Throwable caught) {
		GWT.log("ExceptionTracker", caught);
		LogPanel.getInstance().log(caught);
	}
}
