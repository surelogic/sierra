package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.rules.RulesContent;
import com.surelogic.sierra.gwt.client.usermgmt.UserManagementContent;

public class ContentRegistry {
	private static final Map contentMap = new HashMap();

	private ContentRegistry() {
		// no instances
	}

	public static void initialize() {
		register("login", LoginContent.getInstance(), GuestHeader.getInstance());

		final UserHeader userHeader = UserHeader.getInstance();
		register("overview", OverviewContent.getInstance(), userHeader);
		register("rules", RulesContent.getInstance(), userHeader);
		register("finding", FindingContent.getInstance(), userHeader);
		register("filterset", FilterSetContent.getInstance(), userHeader);
		register("findingtype", FindingTypeContent.getInstance(), userHeader);
		final AdminHeader adminHeader = AdminHeader.getInstance();
		register("settings", SettingsContent.getInstance(), adminHeader);
		register("usermanagement", UserManagementContent.getInstance(),
				adminHeader);
	}

	public static ContentComposite getContent(String contentName) {
		for (final Iterator it = contentMap.entrySet().iterator(); it.hasNext();) {
			final Map.Entry mapEntry = (Map.Entry) it.next();
			final ContentEntry contentEntry = (ContentEntry) mapEntry
					.getValue();
			if (contentEntry.getName().equals(contentName)) {
				return (ContentComposite) mapEntry.getKey();
			}
		}
		return null;
	}

	public static String getContentName(ContentComposite content) {
		final ContentEntry entry = (ContentEntry) contentMap.get(content);
		return entry != null ? entry.getName() : null;
	}

	public static HeaderComposite getContentHeader(ContentComposite content) {
		final ContentEntry entry = (ContentEntry) contentMap.get(content);
		return entry != null ? entry.getHeader() : null;
	}

	public static String getContentUrl(ContentComposite content) {
		final StringBuffer url = new StringBuffer(GWT.getHostPageBaseURL());
		url.append('#').append(getContentName(content));
		return url.toString();
	}

	private static void register(String contentName, ContentComposite content,
			HeaderComposite header) {
		contentMap.put(content, new ContentEntry(contentName, header));
	}

	private static class ContentEntry {
		private final String name;
		private final HeaderComposite header;

		public ContentEntry(String name, HeaderComposite header) {
			super();
			this.name = name;
			this.header = header;
		}

		public String getName() {
			return name;
		}

		public HeaderComposite getHeader() {
			return header;
		}
	}
}
