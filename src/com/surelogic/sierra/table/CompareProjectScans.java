package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Report.Parameter;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public class CompareProjectScans implements IDatabaseTable {

	public ReportTable generate(final Report report, final Connection conn)
			throws SQLException {
		final Parameter first = report.getParameter("first");
		final Parameter second = report.getParameter("second");
		final ReportTable table = new ReportTable();
		table.getHeaders().addAll(
				Arrays.asList(new String[] { "Id", "Examined", "Last Changed",
						"Importance", "Status", "Line", "Artifacts",
						"Comments", "Package", "Class", "Finding Type", "Tool",
						"Summary", "Compilation" }));
		table.getColumns().addAll(
				Arrays.asList(new ColumnData[] { ColumnData.TEXT,
						ColumnData.TEXT, ColumnData.DATE, ColumnData.TEXT,
						ColumnData.TEXT, ColumnData.NUMBER, ColumnData.NUMBER,
						ColumnData.NUMBER, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.TEXT, ColumnData.TEXT, ColumnData.TEXT,
						ColumnData.TEXT }));
		if ((first != null) && (second != null)) {
			final Query q = new ConnectionQuery(conn);
			final String firstStr = first.getValue();
			final String secondStr = second.getValue();
			if (LangUtil.notEmpty(firstStr) && LangUtil.notEmpty(secondStr)) {
				q.prepared("Plots.Project.compareScans",
						new RowHandler<Void>() {
							public Void handle(final Row row) {
								final List<String> tableRow = new ArrayList<String>(
										14);
								for (int i = 0; i < 14; i++) {
									tableRow.add(row.nextString());
								}
								table.getData().add(tableRow);
								return null;
							}
						}).call(firstStr, secondStr);
			}
		}
		return table;
	}

}
