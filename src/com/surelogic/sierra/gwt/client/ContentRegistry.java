package com.surelogic.sierra.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.finding.FindingContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
import com.surelogic.sierra.gwt.client.content.projects.ProjectsContent;
import com.surelogic.sierra.gwt.client.content.reports.ReportsContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.content.servermgmt.ServerManagementContent;
import com.surelogic.sierra.gwt.client.content.settings.SettingsContent;
import com.surelogic.sierra.gwt.client.content.usermgmt.UserManagementContent;
import com.surelogic.sierra.gwt.client.header.AdminHeader;
import com.surelogic.sierra.gwt.client.header.GuestHeader;
import com.surelogic.sierra.gwt.client.header.HeaderComposite;
import com.surelogic.sierra.gwt.client.header.UserHeader;

public class ContentRegistry {
	private static final Map<ContentComposite, ContentEntry> contentMap = new HashMap<ContentComposite, ContentEntry>();

	private ContentRegistry() {
		// no instances
	}

	public static void initialize() {
		register("login", LoginContent.getInstance(), GuestHeader.getInstance());

		final UserHeader userHeader = UserHeader.getInstance();
		register("overview", OverviewContent.getInstance(), userHeader);
		register("categories", CategoriesContent.getInstance(), userHeader);
		register("finding", FindingContent.getInstance(), userHeader);
		register("projects", ProjectsContent.getInstance(), userHeader);
		register("scanfilters", ScanFiltersContent.getInstance(), userHeader);
		register("findingtype", FindingTypesContent.getInstance(), userHeader);
		register("reports", ReportsContent.getInstance(), userHeader);
		final AdminHeader adminHeader = AdminHeader.getInstance();
		register("settings", SettingsContent.getInstance(), adminHeader);
		register("usermanagement", UserManagementContent.getInstance(),
				adminHeader);
		register("servermanagement", ServerManagementContent.getInstance(),
				adminHeader);
	}

	public static ContentComposite getContent(String contentName) {
		for (final Map.Entry<ContentComposite, ContentEntry> mapEntry : contentMap
				.entrySet()) {
			final ContentEntry contentEntry = mapEntry.getValue();
			if (contentEntry.getName().equals(contentName)) {
				return mapEntry.getKey();
			}
		}
		return null;
	}

	public static String getContentName(ContentComposite content) {
		final ContentEntry entry = contentMap.get(content);
		return entry != null ? entry.getName() : null;
	}

	public static HeaderComposite getContentHeader(ContentComposite content) {
		final ContentEntry entry = contentMap.get(content);
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
