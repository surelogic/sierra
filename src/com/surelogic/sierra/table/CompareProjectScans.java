package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CompareProjectScans implements IDatabaseTable {

	private static final int MAX_RESULTS = 250;

	public ReportTable generate(final ReportSettings report,
			final Connection conn) throws SQLException {
		final List<String> scans = report.getSettingValue("scans");
		final ReportTable table = new ReportTable();
		table.getHeaders().addAll(
				Arrays.asList(new String[] { "Id", "Package", "Compilation",
						"Line", "Finding Type", "Tool", "Examined",
						"Last Changed", "Importance", "Status", "Artifacts",
						"Comments", "Summary", "Class" }));
		table.getColumns().addAll(
				Arrays.asList(new ColumnData[] { ColumnData.NUMBER,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.NUMBER,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.DATE, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.NUMBER, ColumnData.NUMBER, ColumnData.TEXT,
						ColumnData.TEXT }));
		if ((scans != null) && (scans.size() >= 2)) {
			final Query q = new ConnectionQuery(conn);
			final String firstScan = scans.get(0);
			final String secondScan = scans.get(1);
			if (LangUtil.notEmpty(firstScan) && LangUtil.notEmpty(secondScan)) {
				q.prepared("Plots.Project.compareScans",
						new ResultHandler<Void>() {

							public Void handle(final Result result) {
								int count = 0;
								for (final Row row : result) {
									if (count++ > MAX_RESULTS) {
										return null;
									}
									final List<String> tableRow = new ArrayList<String>(
											14);
									for (int i = 0; i < 14; i++) {
										tableRow.add(row.nextString());
									}
									table.getData().add(tableRow);
								}
								return null;
							}
						}).call(firstScan, secondScan);
			}
		}
		return table;
	}

}
