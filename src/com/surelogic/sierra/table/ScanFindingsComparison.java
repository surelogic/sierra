package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.NullResultHandler;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.util.LangUtil;
import com.surelogic.sierra.util.Dates;

public class ScanFindingsComparison implements IDatabaseTable {

	private static final int MAX_RESULTS = 250;

	public final ReportTable generate(final Report report, final Connection conn)
			throws SQLException {
		final Parameter scans = report.getParameter("scans");
		final ReportTable table = new ReportTable();
		table.getHeaders().addAll(
				Arrays.asList(new String[] { "Id", "Package", "Compilation",
						"Line", "Finding Type", "Tool", "Examined",
						"Last Changed", "Importance", "Artifacts", "Comments",
						"Summary", "Class" }));
		table.getColumns().addAll(
				Arrays.asList(new ColumnData[] { ColumnData.LINK,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.NUMBER,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.DATE, ColumnData.TEXT, ColumnData.NUMBER,
						ColumnData.NUMBER, ColumnData.TEXT, ColumnData.TEXT }));
		if ((scans != null) && (scans.getValues().size() >= 2)) {
			final Query q = new ConnectionQuery(conn);
			final List<String> values = scans.getValues();
			final String firstStr = values.get(0);
			final String secondStr = values.get(1);
			if (LangUtil.notEmpty(firstStr) && LangUtil.notEmpty(secondStr)) {
				q.prepared("Plots.project.scanFindingsDiff",
						new NullResultHandler() {

							@Override
							public void doHandle(final Result result) {
								int count = 0;
								for (final Row row : result) {
									if (count++ > MAX_RESULTS) {
										return;
									}

									final List<String> tableRow = new ArrayList<String>(
											13);
									final String id = row.nextString();
									tableRow.add(id);
									table.getLinks().add(
											"finding/finding=" + id);
									for (int i = 0; i < 6; i++) {
										tableRow.add(row.nextString());
									}
									tableRow.add(Dates.format(row.nextDate()));
									for (int i = 0; i < 5; i++) {
										tableRow.add(row.nextString());
									}
									table.getData().add(tableRow);
								}
							}
						}).call(firstStr, secondStr, firstStr);
			}
		}
		return table;
	}
}
