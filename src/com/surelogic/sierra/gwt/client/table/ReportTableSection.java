package com.surelogic.sierra.gwt.client.table;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;

public class ReportTableSection extends TableSection {
	private ReportSettings report;
	private ReportTable table;

	public ReportTableSection() {
		super();
	}

	public ReportTableSection(final ReportSettings r) {
		super();
		setReport(r);
	}

	@Override
	protected void doInitialize(final FlexTable grid) {
		// nothing to do
	}

	public void setReport(final ReportSettings r) {
		report = r;
		if (r != null) {
			setTitle(report.getTitle());
			setSummary(report.getDescription());
			getReportData();
		}
	}

	private void getReportData() {
		ServiceHelper.getTicketService().getReportTable(report,
				new ResultCallback<ReportTable>() {

					@Override
					protected void doFailure(final String message,
							final ReportTable result) {
						setErrorStatus(message);
					}

					@Override
					protected void doSuccess(final String message,
							final ReportTable result) {
						table = result;
						setHeaderTitles(table.getHeaders());
						setColumnTypes(table.getColumns());
						final List<List<String>> rows = table.getData();
						if (rows.isEmpty()) {
							setSuccessStatus("No information to display");
						} else {
							clearRows();
							for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
								addRow();
								final List<String> columns = rows.get(rowIndex);
								for (int colIndex = 0; colIndex < columns
										.size(); colIndex++) {
									final String columnText = columns
											.get(colIndex);
									if (getColumnType(colIndex) == ColumnDataType.LINK) {
										// table.getLinks().get(currentRow)
										addColumn(columnText, table.getLinks()
												.get(rowIndex));
									} else {
										addColumn(columnText);
									}
								}
							}
						}

					}
				});
	}

}
