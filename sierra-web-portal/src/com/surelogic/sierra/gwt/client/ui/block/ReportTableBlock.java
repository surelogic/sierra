package com.surelogic.sierra.gwt.client.ui.block;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.ReportTable;
import com.surelogic.sierra.gwt.client.service.ServiceHelper;
import com.surelogic.sierra.gwt.client.service.callback.ResultCallback;
import com.surelogic.sierra.gwt.client.ui.TableBuilder;
import com.surelogic.sierra.gwt.client.ui.type.Status;
import com.surelogic.sierra.gwt.client.ui.type.Status.State;

public class ReportTableBlock extends ReportBlock<FlexTable> {

	public ReportTableBlock() {
		super(new FlexTable());
	}

	public ReportTableBlock(final ReportSettings report) {
		super(new FlexTable(), report);
	}

	@Override
	protected final void reportChanged(final ReportSettings report) {
		ServiceHelper.getTicketService().getReportTable(report,
				new ResultCallback<ReportTable>() {

					@Override
					protected void doFailure(final String message,
							final ReportTable result) {
						setStatus(new Status(State.FAILURE, message));
					}

					@Override
					protected void doSuccess(final String message,
							final ReportTable result) {
						updateTable(result);
					}

				});
	}

	private void updateTable(final ReportTable table) {
		final FlexTable root = getRoot();
		root.clear();

		final TableBuilder tb = new TableBuilder(root);
		tb.setHeaderTitles(table.getHeaders());
		tb.setColumnTypes(table.getColumns());
		final List<List<String>> rows = table.getData();
		if (rows.isEmpty()) {
			setStatus(new Status(State.SUCCESS, "No information to display"));
		} else {
			setStatus(null);
			tb.clearRows();
			for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
				tb.addRow();
				final List<String> columns = rows.get(rowIndex);
				for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
					final String columnText = columns.get(colIndex);
					if (tb.getColumnType(colIndex) == ColumnDataType.LINK) {
						tb
								.addColumn(columnText, table.getLinks().get(
										rowIndex));
					} else {
						tb.addColumn(columnText);
					}
				}
			}
		}
	}
}
