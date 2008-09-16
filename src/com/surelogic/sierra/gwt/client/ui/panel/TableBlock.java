package com.surelogic.sierra.gwt.client.ui.panel;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.data.ColumnDataType;
import com.surelogic.sierra.gwt.client.util.LangUtil;

public abstract class TableBlock extends BlockPanel {
	private static final String PRIMARY_STYLE = "sl-TableSection";
	private final FlexTable grid = new FlexTable();
	private ColumnDataType[] columnTypes;
	private int currentRow = 0;
	private int currentColumn = 0;

	@Override
	protected final void onInitialize(final VerticalPanel contentPanel) {
		grid.setWidth("100%");
		grid.addStyleName(PRIMARY_STYLE);

		doInitialize(grid);
	}

	protected abstract void doInitialize(FlexTable grid);

	protected final void setHeaderTitles(final List<String> titles) {
		setHeaderTitles(titles.toArray(new String[titles.size()]));
	}

	protected final void setHeaderTitles(final String[] titles) {
		for (int i = 0; i < titles.length; i++) {
			grid.setText(0, i, titles[i]);
		}

		grid.getRowFormatter().setStyleName(0, PRIMARY_STYLE + "-header");
	}

	protected final void setColumnTypes(final List<ColumnDataType> headerTypes) {
		setColumnTypes(headerTypes.toArray(new ColumnDataType[headerTypes
				.size()]));
	}

	protected final void setColumnTypes(final ColumnDataType[] headerTypes) {
		this.columnTypes = headerTypes;
	}

	protected final ColumnDataType getColumnType(final int column) {
		return columnTypes != null && column < columnTypes.length ? columnTypes[column]
				: null;
	}

	protected final void clearRows() {
		getContentPanel().remove(grid);
		while (grid.getRowCount() > 1) {
			grid.removeRow(1);
		}

		currentRow = 0;
		currentColumn = 0;
	}

	protected final void addRow() {
		final VerticalPanel contentPanel = getContentPanel();
		if (contentPanel.getWidgetIndex(grid) == -1) {
			contentPanel.add(grid);
		}

		currentRow++;
		currentColumn = 0;

		grid.getRowFormatter()
				.addStyleName(currentRow, PRIMARY_STYLE + "-data");
	}

	protected final void addColumn(final Widget widget) {
		if (currentRow == 0) {
			addRow();
		}

		grid.setWidget(currentRow, currentColumn, widget);

		final ColumnDataType columnType = getColumnType(currentColumn);
		if (columnType != null) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					columnType.getCSS());
		}

		currentColumn++;
	}

	protected final void addColumn(final String text) {
		if (currentRow == 0) {
			addRow();
		}

		grid.setText(currentRow, currentColumn, text);

		final ColumnDataType columnType = getColumnType(currentColumn);
		if (columnType != null) {
			grid.getCellFormatter().addStyleName(currentRow, currentColumn,
					columnType.getCSS());
		}

		currentColumn++;
	}

	protected final void addColumn(final String text, final String linkUrl) {
		if (getColumnType(currentColumn) == ColumnDataType.LINK) {
			addColumn(new Hyperlink(text, linkUrl));
		} else {
			addColumn(text);
		}
	}

	protected final void addColumn(final int value) {
		addColumn(LangUtil.emptyZeroString(value));
	}

}
