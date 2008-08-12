package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.util.Dates;

public class ScanFindings implements IDatabaseTable {

	private static final int MAX_RESULTS = 250;

	public ReportTable generate(final ReportSettings report,
			final Connection conn) throws SQLException {
		final ReportTable table = new ReportTable();
		table.getHeaders().addAll(
				Arrays.asList(new String[] { "Id", "Package", "Compilation",
						"Line", "Finding Type", "Tool", "Examined",
						"Last Changed", "Importance", "Artifacts", "Comments",
						"Summary" }));
		table.getColumns().addAll(
				Arrays.asList(new ColumnData[] { ColumnData.LINK,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.NUMBER,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.DATE, ColumnData.TEXT, ColumnData.NUMBER,
						ColumnData.NUMBER, ColumnData.TEXT }));
		final String scan = report.getSettingValue("scan", 0);
		final List<String> importanceList = report.getSettingValue("importance");
		final List<String> packages = report.getSettingValue("package");
		if (scan != null) {
			if ((scan != null) && !(scan.length() == 0)) {
				final List<Importance> importances;
				if (importanceList == null || importanceList.isEmpty()) {
					importances = Importance.standardValues();
				} else {
					importances = new ArrayList<Importance>();
					for (final String imp : importanceList) {
						importances.add(Importance.fromValue(imp));
					}
				}
				final StringBuilder impStr = new StringBuilder();
				for (final Importance i : importances) {
					impStr.append(i.ordinal());
					impStr.append(",");
				}
				impStr.setLength(impStr.length() - 1);
				if (packages != null && !packages.isEmpty()) {
					final StringBuilder packageStr = new StringBuilder();
					for (final String pakkage : packages) {
						packageStr.append("'");
						packageStr.append(JDBCUtils.escapeString(pakkage));
						packageStr.append("'");
						packageStr.append(",");
					}
					packageStr.setLength(packageStr.length() - 1);
					new ConnectionQuery(conn).statement(
							"Plots.Scan.scanFindings",
							new ResultHandler<Void>() {
								public Void handle(final Result result) {
									int count = 0;
									for (final Row row : result) {
										if (count++ > MAX_RESULTS) {
											return null;
										}
										final List<String> tableRow = new ArrayList<String>(
												14);
										final String id = row.nextString();
										tableRow.add(id);
										table.getLinks().add(
												"finding/finding=" + id);
										for (int i = 0; i < 6; i++) {
											tableRow.add(row.nextString());
										}
										tableRow.add(Dates.format(row
												.nextDate()));
										for (int i = 0; i < 4; i++) {
											tableRow.add(row.nextString());
										}
										table.getData().add(tableRow);
									}
									return null;
								}
							}).call(JDBCUtils.escapeString(scan),
							packageStr.toString(), impStr.toString());

				}
			}
		}
		return table;
	}
}
