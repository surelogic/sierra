package com.surelogic.sierra.gwt.client.table;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.ReportTable.ColumnData;

public class ReportTableSection extends TableSection {

	private final ReportTable report;

	public ReportTableSection(ReportTable report) {
		this.report = report;
		setTitle(report.getReport().getTitle());
		setSummary(report.getReport().getDescription());
	}

	@Override
	protected String[] getHeaderDataTypes() {
		final List<String> css = new ArrayList<String>();
		for (final ColumnData c : report.getColumns()) {
			css.add(c.getCSS());
		}
		return css.toArray(new String[css.size()]);
	}

	@Override
	protected String[] getHeaderTitles() {
		return report.getHeaders().toArray(
				new String[report.getHeaders().size()]);
	}

	@Override
	protected void updateTable(Context context) {
		clearRows();
		final List<List<String>> rows = report.getData();
		if (rows.isEmpty()) {
			setSuccessStatus("No information to display");
		} else {
			for (final List<String> row : rows) {
				addRow();
				for (final String col : row) {
					addColumn(col);
				}
			}
		}
	}

}
