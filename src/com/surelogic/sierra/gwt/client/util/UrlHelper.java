package com.surelogic.sierra.gwt.client.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.http.client.URL;

public final class UrlHelper {
	public static final String UNIQUE_REQUEST_ID = "ureqid";

	private UrlHelper() {
		// no instances
	}

	public static String encodeParameters(Map parameters) {
		StringBuffer b = new StringBuffer();
		for (Iterator i = parameters.entrySet().iterator(); i.hasNext();) {
			final Entry e = (Entry) i.next();
			final String prop = (String) e.getKey();
			final String val = (String) e.getValue();
			b.append(URL.encodeComponent(prop));
			b.append("=");
			b.append(URL.encodeComponent(val));
			b.append("&");
		}
		return b.toString();
	}

	public static String appendParameters(String url, Map parameters) {
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

	public static void setUniqueRequestId(Map parameters) {
		parameters.put(UNIQUE_REQUEST_ID, getUniqueRequestId());
	}

	public static String appendUniqueRequestId(String url) {
		return appendParameter(url, UNIQUE_REQUEST_ID, getUniqueRequestId());
	}
}
