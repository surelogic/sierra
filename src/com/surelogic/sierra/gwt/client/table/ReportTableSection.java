package com.surelogic.sierra.gwt.client.table;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ReportTable.ColumnData;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class ReportTableSection extends SectionPanel {

	private Report report;

	public ReportTableSection(Report r) {
		setReport(r);
	}

	public ReportTableSection() {

	}

	public void setReport(Report r) {
		report = r;
		if (r != null) {
			setTitle(report.getTitle());
			setSummary(report.getDescription());
			getReportData();
		}
	}

	private void getReportData() {
		ServiceHelper.getTicketService().getReportTable(report,
				new AsyncCallback<Result<ReportTable>>() {

					public void onFailure(Throwable caught) {

					}

					public void onSuccess(Result<ReportTable> result) {
						if (result.isSuccess()) {
							final ReportTable table = result.getResult();
							final List<String> headerTitles = table
									.getHeaders();
							final List<ColumnData> columnType = table
									.getColumns();
							for (int i = 0; i < headerTitles.size(); i++) {
								grid.setText(0, i, headerTitles.get(i));
							}

							grid.getRowFormatter().setStyleName(0,
									PRIMARY_STYLE + "-header");
							final List<List<String>> rows = table.getData();
							if (rows.isEmpty()) {
								setSuccessStatus("No information to display");
							} else {
								clearRows();
								for (final List<String> row : rows) {
									addRow();
									int i = 0;
									for (final String col : row) {
										addColumn(col, columnType.get(i++));
									}
								}
							}
						}

					}
				});
	}

	private static final String PRIMARY_STYLE = "sl-TableSection";
	private final FlexTable grid = new FlexTable();
	private int currentRow = 0;
	private int currentColumn = 0;

	protected final void onInitialize(VerticalPanel contentPanel) {
		grid.setWidth("100%");
		grid.addStyleName(PRIMARY_STYLE);

	}

	protected final void onUpdate(Context context) {
		if (report != null) {
			getReportData();
		}
	}

	protected final void onDeactivate() {
		clearRows();
	}

	protected void clearRows() {
		getContentPanel().remove(grid);
		while (grid.getRowCount() > 1) {
			grid.removeRow(1);
		}

		currentRow = 0;
		currentColumn = 0;
	}

	protected void addRow() {
		final VerticalPanel contentPanel = getContentPanel();
		if (contentPanel.getWidgetIndex(grid) == -1) {
			contentPanel.add(grid);
		}

		currentRow++;
		currentColumn = 0;

		grid.getRowFormatter()
				.addStyleName(currentRow, PRIMARY_STYLE + "-data");
	}

	protected void addColumn(String text, ColumnData type) {
		if (currentRow == 0) {
			addRow();
		}

		grid.setText(currentRow, currentColumn, text);

		if (type != null) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					type.getCSS());
		}

		currentColumn++;
	}

}
