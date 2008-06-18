package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.UserOverview;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.portal.PortalOverview;

public class UserAudits implements IDatabaseTable {

	public static final List<String> HEADERS = new ArrayList<String>(Arrays
			.asList(new String[] { "User", "Audits", "Last Contribution" }));
	public static final List<ColumnData> COLUMNS = new ArrayList<ColumnData>(
			Arrays.asList(new ColumnData[] { ColumnData.TEXT,
					ColumnData.NUMBER, ColumnData.DATE }));

	public ReportTable generate(Report report, Connection conn)
			throws SQLException {
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final ReportTable table = new ReportTable(report);
		final Parameter disabled = report.getParameter("show-disabled");
		final boolean showDisabled = (disabled != null)
				&& "true".equalsIgnoreCase(disabled.getValue());
		final PortalOverview po = PortalOverview.getInstance(conn);
		final List<UserOverview> overviews = showDisabled ? po
				.getUserOverviews() : po.getEnabledUserOverviews();
		table.getHeaders().addAll(HEADERS);
		table.getColumns().addAll(COLUMNS);
		for (final UserOverview u : overviews) {
			table.getData().add(
					new ArrayList<String>(Arrays
							.asList(new String[] {
									u.getUserName(),
									u.getAudits() > 0 ? Integer.toString(u
											.getAudits())
											+ " on "
											+ Integer.toString(u.getFindings())
											+ " findings." : "None",
									u.getLastSynch() == null ? "-" : u
											.getLastSynch() })));
		}
		return table;
	}
}
