package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.GWT;

public class ContentRegistry {
	private static final Map contentMap = new HashMap();

	private ContentRegistry() {
		// no instances
	}

	public static void initialize() {
		// TODO bind each content to a header composite
		register("login", LoginContent.getInstance());
		register("overview", OverviewContent.getInstance());
		register("settings", SettingsContent.getInstance());
		register("usermanagement", UserManagementContent.getInstance());
		register("finding", FindingContent.getInstance());
		register("filterset", FilterSetContent.getInstance());
	}

	public static ContentComposite getContent(String contentName) {
		return (ContentComposite) contentMap.get(contentName);
	}

	public static String getContentName(ContentComposite content) {
		for (Iterator it = contentMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry contentEntry = (Map.Entry) it.next();
			if (contentEntry.getValue().equals(content)) {
				return (String) contentEntry.getKey();
			}
		}
		return null;
	}

	public static String getContentUrl(ContentComposite content) {
		StringBuffer url = new StringBuffer(GWT.getHostPageBaseURL());
		url.append('#').append(getContentName(content));
		return url.toString();
	}

	private static void register(String contentName, ContentComposite content) {
		contentMap.put(contentName, content);
	}
}
