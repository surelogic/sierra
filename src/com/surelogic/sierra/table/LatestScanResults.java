package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.portal.PortalOverview;

public class LatestScanResults implements IDatabaseTable {

	private static final List<String> HEADERS = Arrays
			.asList(new String[] { "Project", "Critical", "High", "Medium",
					"Low", "Total", "Audited" });
	private static final List<ColumnData> COLUMNS = Arrays
			.asList(new ColumnData[] { ColumnData.TEXT, ColumnData.NUMBER,
					ColumnData.NUMBER, ColumnData.NUMBER, ColumnData.NUMBER,
					ColumnData.NUMBER, ColumnData.NUMBER });

	public ReportTable generate(ReportSettings report, Connection c)
			throws SQLException {
		c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final List<ProjectOverview> overview = PortalOverview.getInstance(c)
				.getProjectOverviews();
		Collections.sort(overview, new Comparator<ProjectOverview>() {
			public int compare(ProjectOverview o1, ProjectOverview o2) {
				return o2.getTotalFindings() - o1.getTotalFindings();
			}
		});
		final ReportTable t = new ReportTable(report);
		t.getHeaders().addAll(HEADERS);
		t.getColumns().addAll(COLUMNS);
		for (final ProjectOverview po : overview) {
			final List<String> row = new ArrayList<String>(HEADERS.size());
			row.add(po.getName());
			row.add(Integer.toString(po.getCritical()));
			row.add(Integer.toString(po.getHigh()));
			row.add(Integer.toString(po.getMedium()));
			row.add(Integer.toString(po.getLow()));
			row.add(Integer.toString(po.getTotalFindings()));
			row.add(Integer.toString(po.getCommentedFindings()));
			t.getData().add(row);
		}
		return t;
	}
}
