package com.surelogic.sierra.gwt.client.content;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.surelogic.sierra.gwt.client.content.categories.CategoriesContent;
import com.surelogic.sierra.gwt.client.content.finding.FindingContent;
import com.surelogic.sierra.gwt.client.content.findingtypes.FindingTypesContent;
import com.surelogic.sierra.gwt.client.content.login.LoginContent;
import com.surelogic.sierra.gwt.client.content.overview.OverviewContent;
import com.surelogic.sierra.gwt.client.content.projects.ProjectsContent;
import com.surelogic.sierra.gwt.client.content.reports.TeamServerReportsContent;
import com.surelogic.sierra.gwt.client.content.scanfilters.ScanFiltersContent;
import com.surelogic.sierra.gwt.client.content.scans.ScanContent;
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
		register("login", "Login", LoginContent.getInstance(), GuestHeader
				.getInstance());

		final UserHeader userHeader = UserHeader.getInstance();
		register("overview", "Welcome", OverviewContent.getInstance(),
				userHeader);
		register("categories", "Categories", CategoriesContent.getInstance(),
				userHeader);
		register("finding", "Finding", FindingContent.getInstance(), userHeader);
		register("projects", "Projects", ProjectsContent.getInstance(),
				userHeader);
		register("scans", "Scans", ScanContent.getInstance(), userHeader);
		register("scanfilters", "Scan Filters", ScanFiltersContent
				.getInstance(), userHeader);
		register("findingtype", "Finding Types", FindingTypesContent
				.getInstance(), userHeader);
		register("tsreports", "Reports",
				TeamServerReportsContent.getInstance(), userHeader);
		final AdminHeader adminHeader = AdminHeader.getInstance();
		register("settings", "Settings", SettingsContent.getInstance(),
				adminHeader);
		register("usermanagement", "Users",
				UserManagementContent.getInstance(), adminHeader);
		register("servermanagement", "Servers", ServerManagementContent
				.getInstance(), adminHeader);
	}

	public static ContentComposite getContent(final String contentName) {
		for (final Map.Entry<ContentComposite, ContentEntry> mapEntry : contentMap
				.entrySet()) {
			final ContentEntry contentEntry = mapEntry.getValue();
			if (contentEntry.getName().equals(contentName)) {
				return mapEntry.getKey();
			}
		}
		return null;
	}

	public static String getContentName(final ContentComposite content) {
		final ContentEntry entry = contentMap.get(content);
		return entry != null ? entry.getName() : null;
	}

	public static String getContentTitle(final ContentComposite content) {
		final ContentEntry entry = contentMap.get(content);
		return entry != null ? entry.getTitle() : null;
	}

	public static HeaderComposite getContentHeader(
			final ContentComposite content) {
		final ContentEntry entry = contentMap.get(content);
		return entry != null ? entry.getHeader() : null;
	}

	public static String getContentUrl(final ContentComposite content) {
		final StringBuffer url = new StringBuffer(GWT.getHostPageBaseURL());
		url.append('#').append(getContentName(content));
		return url.toString();
	}

	private static void register(final String contentName,
			final String contentTitle, final ContentComposite content,
			final HeaderComposite header) {
		contentMap.put(content, new ContentEntry(contentName, contentTitle,
				header));
	}

	private static class ContentEntry {
		private final String name;
		private final String title;
		private final HeaderComposite header;

		public ContentEntry(final String name, final String title,
				final HeaderComposite header) {
			super();
			this.name = name;
			this.title = title;
			this.header = header;
		}

		public String getName() {
			return name;
		}

		public String getTitle() {
			return title;
		}

		public HeaderComposite getHeader() {
			return header;
		}
	}
}
