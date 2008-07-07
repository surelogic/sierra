package com.surelogic.sierra.gwt.client.table;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.Context;
import com.surelogic.sierra.gwt.client.data.ColumnData;
import com.surelogic.sierra.gwt.client.ui.SectionPanel;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class TableSection extends SectionPanel {
	private static final String PRIMARY_STYLE = "sl-TableSection";
	private final FlexTable grid = new FlexTable();
	private int currentRow = 0;
	private int currentColumn = 0;

	@Override
	protected final void onInitialize(final VerticalPanel contentPanel) {
		grid.setWidth("100%");
		grid.addStyleName(PRIMARY_STYLE);

		final String[] headerTitles = getHeaderTitles();
		for (int i = 0; i < headerTitles.length; i++) {
			grid.setText(0, i, headerTitles[i]);
		}

		grid.getRowFormatter().setStyleName(0, PRIMARY_STYLE + "-header");
	}

	@Override
	protected final void onUpdate(final Context context) {
		updateTable(context);
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

	protected void addColumn(final Widget widget) {
		if (currentRow == 0) {
			addRow();
		}

		grid.setWidget(currentRow, currentColumn, widget);

		final ColumnData[] headerTypes = getHeaderDataTypes();
		if (currentColumn < headerTypes.length) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					headerTypes[currentColumn].getCSS());
		}

		currentColumn++;
	}

	protected void addColumn(final String text) {
		if (currentRow == 0) {
			addRow();
		}

		grid.setText(currentRow, currentColumn, text);

		final ColumnData[] headerTypes = getHeaderDataTypes();
		if (currentColumn < headerTypes.length) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					headerTypes[currentColumn].getCSS());
		}

		currentColumn++;
	}

	protected void addColumn(final int value) {
		addColumn(LangUtil.emptyZeroString(value));
	}

	protected abstract String[] getHeaderTitles();

	protected abstract ColumnData[] getHeaderDataTypes();

	protected abstract void updateTable(Context context);

}
