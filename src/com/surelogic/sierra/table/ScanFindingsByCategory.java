package com.surelogic.sierra.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.JDBCUtils;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Result;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryGraph;
import com.surelogic.sierra.tool.message.Importance;

public class ScanFindingsByCategory implements IDatabaseTable {

	private static final int MAX_RESULTS = 250;

	public ReportTable generate(final ReportSettings report,
			final Connection conn) throws SQLException {
		final ReportTable table = new ReportTable();
		table.getHeaders().addAll(
				Arrays.asList(new String[] { "Id", "Compilation", "Line",
						"Importance", "Summary" }));
		table.getColumns().addAll(
				Arrays.asList(new ColumnDataType[] { ColumnDataType.LINK,
						ColumnDataType.TEXT, ColumnDataType.NUMBER,
						ColumnDataType.TEXT, ColumnDataType.TEXT, }));
		final String scan = report.getSettingValue("scan", 0);
		final List<String> importanceList = report
				.getSettingValue("importance");
		final List<String> packages = report.getSettingValue("package");
		final List<String> categories = report.getSettingValue("category");
		if (scan == null || scan.length() == 0) {
			return null;
		}
		final Query q = new ConnectionQuery(conn);
		final Categories cats = new Categories(q);
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
			final StringBuilder ftStr = new StringBuilder();
			final List<CategoryGraph> graphs = cats
					.getCategoryGraphs(categories);
			final HashSet<String> findingTypes = new HashSet<String>();
			for (final CategoryGraph cg : graphs) {
				findingTypes.addAll(cg.getFindingTypes());
			}
			if (findingTypes.isEmpty()) {
				ftStr.append("''");
			} else {
				for (final String findingType : findingTypes) {
					ftStr.append("'");
					ftStr.append(JDBCUtils.escapeString(findingType));
					ftStr.append("',");
				}
				ftStr.setLength(ftStr.length() - 1);
			}
			q.statement("Plots.Scan.scanFindingsByCategory",
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
								table.getLinks().add("finding/finding=" + id);
								final String comp = row.nextString() + "."
										+ row.nextString();
								tableRow.add(comp);
								for (int i = 0; i < 3; i++) {
									tableRow.add(row.nextString());
								}
								table.getData().add(tableRow);
							}
							return null;
						}
					}).call(JDBCUtils.escapeString(scan),
					packageStr.toString(), ftStr.toString(), impStr.toString());
		}
		return table;
	}
}
