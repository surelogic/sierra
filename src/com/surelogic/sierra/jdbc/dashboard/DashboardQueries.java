package com.surelogic.sierra.jdbc.dashboard;

import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
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
			public DashboardSettings perform(final Query query,
					final Server server, final User user) {
				final DashboardSettings s = new DashboardSettings();
				query.prepared("Dashboard.selectReports", new NullRowHandler() {
					int rowNum = -1;

					@Override
					protected void doHandle(final Row r) {
						final int thisRow = r.nextInt();
						final String reportSettings = r.nextString();
						final OutputType out = OutputType.values()[r.nextInt()];
						if (thisRow != rowNum) {
							rowNum = thisRow;
							s.addRow();
						}
						new ReportWidget(ReportSettingQueries
								.getUserReportSettings(reportSettings).perform(
										query, server, user), out);
					}
				}).call(user.getId());
				return null;
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
						insertReport.call(user.getId(), i, j, report
								.getSettings().getUuid(), report
								.getOutputType());
					}
				}
			}
		};
	}

}
