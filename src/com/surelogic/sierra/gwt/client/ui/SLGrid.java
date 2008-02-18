package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

public class SLGrid extends Composite {
	private static final String PRIMARY_STYLE = "sl-Grid";

	private final Grid grid = new Grid(1, 1);
	private boolean rowSelection;
	private CheckBox selectAll;

	public SLGrid(boolean rowSelection) {
		super();
		this.rowSelection = rowSelection;

		initWidget(grid);
		grid.addStyleName(PRIMARY_STYLE);

		grid.getRowFormatter().addStyleName(0, PRIMARY_STYLE + "-headerrow");

		if (rowSelection) {
			selectAll = new CheckBox();
			selectAll.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					setSelectAll(selectAll.isChecked());
				}

			});
			grid.resizeColumns(2);
			grid.setWidget(0, 0, selectAll);
			grid.getCellFormatter().addStyleName(0, 0,
					PRIMARY_STYLE + "-header");
		}
	}

	public int getColumnCount() {
		return grid.getColumnCount() - getColumnOffset();
	}

	public void setColumn(int column, String text, String width) {
		column += getColumnOffset();

		if (column >= grid.getColumnCount()) {
			grid.resizeColumns(column + 1);
		}
		grid.setText(0, column, text);
		grid.getColumnFormatter().setWidth(column, width);
		grid.getCellFormatter().addStyleName(0, column,
				PRIMARY_STYLE + "-header");
	}

	public int getRowCount() {
		return grid.getRowCount() - getRowOffset();
	}

	public int addRow() {
		int rowIndex = grid.getRowCount();
		grid.resizeRows(rowIndex + 1);
		if (rowSelection) {
			grid.setWidget(rowIndex, 0, new CheckBox());
		}
		grid.getRowFormatter().addStyleName(rowIndex,
				PRIMARY_STYLE + "-itemrow");
		for (int i = 0; i < grid.getColumnCount(); i++) {
			grid.getCellFormatter().addStyleName(rowIndex, i,
					PRIMARY_STYLE + "-item");
		}
		return rowIndex - getRowOffset();
	}

	public void removeRows() {
		grid.resizeRows(getRowOffset());
	}

	public Widget getWidget(int row, int column) {
		return grid.getWidget(row + getRowOffset(), column + getColumnOffset());
	}

	public void setWidget(int row, int column, Widget widget) {
		grid
				.setWidget(row + getRowOffset(), column + getColumnOffset(),
						widget);
	}

	public void resizeRows(int rowCount) {
		grid.resizeRows(rowCount + getRowOffset());
		// TODO need to update row to create checkboxes, etc. getting a null ptr

	}

	public void clearRow(int row) {
		setSelected(row, false);
		int colCount = grid.getColumnCount();
		int offsetRow = row + getRowOffset();
		for (int i = getColumnOffset(); i < colCount; i++) {
			grid.setText(offsetRow, i, "");
		}
	}

	public void setSelectAll(boolean selected) {
		if (rowSelection) {
			((CheckBox) grid.getWidget(0, 0)).setChecked(selected);
			int rowCount = getRowCount();
			for (int i = 0; i < rowCount; i++) {
				setSelected(i, selected);
			}
		}
	}

	public boolean isSelected(int row) {
		if (rowSelection) {
			return ((CheckBox) grid.getWidget(row + getRowOffset(), 0))
					.isChecked();
		}
		return false;
	}

	public void setSelected(int row, boolean selected) {
		if (rowSelection) {
			((CheckBox) grid.getWidget(row + getRowOffset(), 0))
					.setChecked(selected);
		}
	}

	public void clearSelections() {
		if (rowSelection) {
			int rowCount = getRowCount();
			for (int i = 0; i < rowCount; i++) {
				setSelected(i, false);
			}
		}
	}

	public void setWaitStatus() {
		removeRows();
		int row = addRow();
		setWidget(row, 0, ImageHelper.getWaitImage(16));
	}

	public void setErrorMessage(String errorText) {
		removeRows();
		Label errorMessage = new Label(errorText, true);
		errorMessage.addStyleName(PRIMARY_STYLE + "-error");
		int row = addRow();
		setWidget(row, 0, errorMessage);
	}

	private int getColumnOffset() {
		return rowSelection ? 1 : 0;
	}

	private int getRowOffset() {
		// will always have a header for now
		return 1;
	}
}
