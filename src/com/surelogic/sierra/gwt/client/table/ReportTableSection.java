package com.surelogic.sierra.gwt.client.table;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.data.Report;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.service.ResultCallback;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;

public class ReportTableSection extends SectionPanel {

	private Report report;
	private ReportTable table;

	public ReportTableSection(final Report r) {
		super();
		setReport(r);
	}

	public ReportTableSection() {
		super();
	}

	private static final String PRIMARY_STYLE = "sl-TableSection";
	private final FlexTable grid = new FlexTable();
	private int currentRow = 0;
	private int currentColumn = 0;

	@Override
	protected final void onInitialize(final VerticalPanel contentPanel) {
		grid.setWidth("100%");
		grid.addStyleName(PRIMARY_STYLE);

	}

	@Override
	protected final void onUpdate(final Context context) {
		if (report != null) {
			getReportData();
		}
	}

	@Override
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

	protected void addColumn(final String text, final ColumnData type) {
		if (currentRow == 0) {
			addRow();
		}

		if (type == ColumnData.LINK) {
			grid.setWidget(currentRow, currentColumn, new Hyperlink(text, table
					.getLinks().get(currentRow)));
		} else {
			grid.setText(currentRow, currentColumn, text);
		}
		if (type != null) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					type.getCSS());
		}

		currentColumn++;
	}

	public void setReport(final Report r) {
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
					protected void doFailure(String message, ReportTable result) {
						setErrorStatus(message);
					}

					@Override
					protected void doSuccess(String message,
							final ReportTable result) {

						table = result;
						final List<String> headerTitles = table.getHeaders();
						final List<ColumnData> columnType = table.getColumns();
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
				});
	}
}
