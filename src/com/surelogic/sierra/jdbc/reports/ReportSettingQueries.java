package com.surelogic.sierra.jdbc.reports;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.NullResultHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.gwt.client.data.ReportSetting;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.jdbc.server.NullUserQuery;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.user.User;

/**
 * Queries that handle saving and loading report settings for users.
 * 
 * @author nathan
 * 
 */
public class ReportSettingQueries {

	public static UserQuery<Void> save(final ReportSettings settings) {
		return new NullUserQuery() {

			@Override
			public void doPerform(final Query query, final Server server,
					final User user) {
				query.prepared("ReportSettings.delete")
						.call(settings.getUuid());
				query.prepared("ReportSettings.insert").call(user.getId(),
						settings.getUuid(), settings.getReportUuid(),
						settings.getTitle(), settings.getDescription());
				for (final ReportSetting param : settings.getSettingParams()) {
					final List<String> values = param.getValues();
					for (int i = 0; i < values.size(); i++) {
						query.prepared("ReportSettings.insertParam").call(
								settings.getUuid(), param.getName(),
								values.get(i), i);
					}
				}
			}
		};
	}

	public static UserQuery<List<ReportSettings>> listUserQueries() {
		return new UserQuery<List<ReportSettings>>() {
			public List<ReportSettings> perform(final Query query,
					final Server server, final User user) {
				return query.prepared("ReportSettings.listUserSettings",
						new ReportSettingsHandler(query)).call(user.getId());
			}
		};
	}

	private static class ReportSettingsHandler implements
			RowHandler<ReportSettings> {

		private final Query query;

		ReportSettingsHandler(final Query query) {
			this.query = query;
		}

		public ReportSettings handle(final Row r) {
			final ReportSettings rs = new ReportSettings();
			rs.setUuid(r.nextString());
			rs.setReportUuid(r.nextString());
			rs.setTitle(r.nextString());
			rs.setDescription(r.nextString());
			query.prepared("ReportSettings.listSettingParams",
					new ReportParamsHandler(rs)).call(rs.getUuid());
			return rs;
		}
	}

	private static class ReportParamsHandler extends NullResultHandler {
		private final ReportSettings rs;

		ReportParamsHandler(final ReportSettings rs) {
			this.rs = rs;
		}

		@Override
		public void doHandle(final Result result) {
			String currentName = null;
			List<String> values = null;
			for (final Row row : result) {
				final String name = row.nextString();
				final String value = row.nextString();
				if (!name.equals(currentName)) {
					currentName = name;
					values = new ArrayList<String>();
					rs.setSettingValue(name, values);
				}
				values.add(value);
			}
		}

	}

}
