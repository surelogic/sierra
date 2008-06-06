package com.surelogic.sierra.gwt.client.util;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.http.client.URL;

public final class UrlHelper {
	public static final String UNIQUE_REQUEST_ID = "ureqid";

	private UrlHelper() {
		// no instances
	}

	public static String encodeParameters(Map<String, String> parameters) {
		StringBuffer b = new StringBuffer();
		for (Entry<String, String> e : parameters.entrySet()) {
			b.append(URL.encodeComponent(e.getKey()));
			b.append("=");
			b.append(URL.encodeComponent(e.getValue()));
			b.append("&");
		}
		return b.toString();
	}

	public static String appendParameters(String url,
			Map<String, String> parameters) {
		StringBuffer newUrl = new StringBuffer(url);
		if (url.indexOf('?') == -1) {
			newUrl.append('?');
		}
		newUrl.append(encodeParameters(parameters));
		return newUrl.toString();
	}

	public static String appendParameter(String url, String key, String value) {
		StringBuffer newUrl = new StringBuffer(url);
		if (!url.endsWith("?") && !url.endsWith("&")) {
			if (url.indexOf('?') == -1) {
				newUrl.append('?');
			} else {
				newUrl.append('&');
			}
		}
		newUrl.append(URL.encodeComponent(key));
		newUrl.append("=");
		newUrl.append(URL.encodeComponent(value));
		return newUrl.toString();
	}

	public static String getUniqueRequestId() {
		return Long.toString(System.currentTimeMillis());
	}

	public static void setUniqueRequestId(Map<String, String> parameters) {
		parameters.put(UNIQUE_REQUEST_ID, getUniqueRequestId());
	}

	public static String appendUniqueRequestId(String url) {
		return appendParameter(url, UNIQUE_REQUEST_ID, getUniqueRequestId());
	}
}
