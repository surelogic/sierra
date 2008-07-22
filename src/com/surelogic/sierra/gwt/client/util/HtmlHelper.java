package com.surelogic.sierra.gwt.client.util;

import com.google.gwt.user.client.ui.HTML;

/**
 * Utility class.
 * 
 * @author nathan
 * 
 */
public final class HtmlHelper {

	private HtmlHelper() {
		// singleton
	}

	public static HTML h1(String text) {
		return new HTML("<h1>" + text + "</h1>");
	}

	public static HTML h2(String text) {
		return new HTML("<h2>" + text + "</h2>");
	}

	public static HTML h3(String text) {
		return new HTML("<h3>" + text + "</h3>");
	}

	public static HTML p(String text) {
		return new HTML("<p>" + text + "</p>");
	}

	public static HTML span(String text) {
		return new HTML("<span>" + text + "</span>");
	}
}
