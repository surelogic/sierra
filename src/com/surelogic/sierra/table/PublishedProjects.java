package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import com.surelogic.sierra.gwt.client.data.ProjectOverview;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.ReportTable.ColumnData;
import com.surelogic.sierra.portal.PortalOverview;

public class PublishedProjects implements IDatabaseTable {

	private static final String[] HEADER = new String[] { "Project",
			"Last Scan", "Audits", "Critical", "High", "Medium", "Low",
			"Irrelevant", "Last Audit", "By" };
	private static final ColumnData[] COLUMNS = new ColumnData[] {
			ColumnData.TEXT, ColumnData.DATE, ColumnData.NUMBER,
			ColumnData.NUMBER, ColumnData.NUMBER, ColumnData.NUMBER,
			ColumnData.NUMBER, ColumnData.NUMBER, ColumnData.DATE,
			ColumnData.TEXT };

	public ReportTable generate(Report report, Connection conn)
			throws SQLException {
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		final ReportTable t = new ReportTable(report);
		t.getHeaders().addAll(Arrays.asList(HEADER));
		t.getColumns().addAll(Arrays.asList(COLUMNS));
		for (final ProjectOverview p : PortalOverview.getInstance(conn)
				.getProjectOverviews()) {
			t.getData().add(
					Arrays.asList(new String[] {
							p.getName(),
							p.getLastScanDate(),
							p.getComments() == 0 ? "" : p.getComments()
									+ " on " + p.getCommentedFindings()
									+ " findings",
							Integer.toString(p.getCritical()),
							Integer.toString(p.getHigh()),
							Integer.toString(p.getMedium()),
							Integer.toString(p.getLow()),
							Integer.toString(p.getIrrelevant()),
							p.getLastSynchDate(), p.getLastSynchUser() }));
		}
		return t;
	}

}
