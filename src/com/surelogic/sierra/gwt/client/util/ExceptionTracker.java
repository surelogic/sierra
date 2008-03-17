package com.surelogic.sierra.gwt.client.util;

import com.google.gwt.user.client.Window;

/**
 * TODO Look up standard GWT exception model, this is an unverified approach
 * 
 */
public class ExceptionTracker {

	public static void logException(Throwable caught) {
		// TODO log these traces to a hidden text field?
		String message = caught.getMessage();
		if (message == null) {
			message = caught.toString();
		}
		Window.alert(message);
	}
}
