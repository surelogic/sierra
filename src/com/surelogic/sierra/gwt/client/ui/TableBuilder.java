package com.surelogic.sierra.gwt.client.ui;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public final class TableBuilder {
	private static final String PRIMARY_STYLE = "sl-TableSection";
	private final FlexTable table;
	private ColumnDataType[] columnTypes;
	private int currentRow = 0;
	private int currentColumn = 0;

	public TableBuilder(final FlexTable target) {
		super();
		this.table = target;
		target.setWidth("100%");
		target.addStyleName(PRIMARY_STYLE);
		currentRow = target.getRowCount();
		currentColumn = 0;
	}

	public void setHeaderTitles(final List<String> titles) {
		setHeaderTitles(titles.toArray(new String[titles.size()]));
	}

	public void setHeaderTitles(final String[] titles) {
		for (int i = 0; i < titles.length; i++) {
			table.setText(0, i, titles[i]);
		}

		table.getRowFormatter().setStyleName(0, PRIMARY_STYLE + "-header");
	}

	public void setColumnTypes(final List<ColumnDataType> headerTypes) {
		setColumnTypes(headerTypes.toArray(new ColumnDataType[headerTypes
				.size()]));
	}

	public void setColumnTypes(final ColumnDataType[] headerTypes) {
		this.columnTypes = headerTypes;
	}

	public ColumnDataType getColumnType(final int column) {
		return columnTypes != null && column < columnTypes.length ? columnTypes[column]
				: null;
	}

	public void clearRows() {
		while (table.getRowCount() > 1) {
			table.removeRow(1);
		}

		currentRow = 0;
		currentColumn = 0;
	}

	public void addRow() {
		currentRow++;
		currentColumn = 0;

		table.getRowFormatter().addStyleName(currentRow,
				PRIMARY_STYLE + "-data");
	}

	public void addColumn(final Widget widget) {
		if (currentRow == 0) {
			addRow();
		}

		table.setWidget(currentRow, currentColumn, widget);

		final ColumnDataType columnType = getColumnType(currentColumn);
		if (columnType != null) {
			table.getCellFormatter().addStyleName(currentRow, currentColumn,
					columnType.getCSS());
		}

		currentColumn++;
	}

	public void addColumn(final String text) {
		if (currentRow == 0) {
			addRow();
		}

		table.setText(currentRow, currentColumn, text);

		final ColumnDataType columnType = getColumnType(currentColumn);
		if (columnType != null) {
			table.getCellFormatter().addStyleName(currentRow, currentColumn,
					columnType.getCSS());
		}

		currentColumn++;
	}

	public void addColumn(final String text, final String linkUrl) {
		if (getColumnType(currentColumn) == ColumnDataType.LINK) {
			addColumn(new Hyperlink(text, linkUrl));
		} else {
			addColumn(text);
		}
	}

	public void addColumn(final int value) {
		addColumn(LangUtil.emptyZeroString(value));
	}
}
