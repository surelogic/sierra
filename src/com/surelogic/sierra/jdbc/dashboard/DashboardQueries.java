package com.surelogic.sierra.jdbc.dashboard;

import java.util.UUID;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.ReportWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.jdbc.reports.ReportSettingQueries;
import com.surelogic.sierra.jdbc.server.NullUserQuery;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.user.User;

public class DashboardQueries {

	public static UserQuery<DashboardSettings> getDashboard() {
		return new UserQuery<DashboardSettings>() {
			boolean exists = false;

			public DashboardSettings perform(final Query query,
					final Server server, final User user) {
				final DashboardSettings s = new DashboardSettings();
				query.prepared("Dashboard.selectReports", new NullRowHandler() {

					@Override
					protected void doHandle(final Row r) {
						exists = true;

						final int dbRow = r.nextInt();
						final int column = s.getRow(dbRow, true).getColumns()
								.size();
						final String reportSettings = r.nextString();
						final OutputType out = OutputType.values()[r.nextInt()];

						final ReportWidget widget = reportSettings == null ? null
								: new ReportWidget(ReportSettingQueries
										.getUserReportSettings(reportSettings)
										.perform(query, server, user), out);
						s.setWidget(dbRow, column, widget);
					}
				}).call(user.getId());
				return exists ? s : null;
			}
		};
	}

	public static UserQuery<Void> updateDashboard(
			final DashboardSettings dashboard) {
		return new NullUserQuery() {
			@Override
			public void doPerform(final Query query, final Server server,
					final User user) {
				query.prepared("Dashboard.deleteUserSettings").call(
						user.getId());
				final Queryable<Void> insertReport = query
						.prepared("Dashboard.insertReport");
				int i = 0;
				for (final DashboardRow row : dashboard.getRows()) {
					i++;
					int j = 0;
					for (final DashboardWidget widget : row.getColumns()) {
						j++;
						final ReportWidget report = (ReportWidget) widget;
						Object settingUuid;
						Object outputType;
						if (report != null) {
							final ReportSettings settings = report
									.getSettings();
							if (settings.getUuid() == null) {
								// We haven't saved these settings yet, so go
								// ahead
								// and do that now
								if (settings.getUuid() == null) {
									settings.setUuid(UUID.randomUUID()
											.toString());
								}
								ReportSettingQueries.save(settings).perform(
										query, server, user);
							}
							settingUuid = settings.getUuid();
							outputType = report.getOutputType().ordinal();
						} else {
							settingUuid = Nulls.STRING;
							outputType = Nulls.INT;
						}
						insertReport.call(user.getId(), i, j, settingUuid,
								outputType);
					}
				}
			}
		};
	}

}
