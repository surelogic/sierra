package com.surelogic.sierra.gwt.client.ui;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

// TODO add the ability to bind a data object to each row
public class SLGrid extends Composite {
	private static final String PRIMARY_STYLE = "sl-Grid";

	private final FlexTable grid = new FlexTable();
	private boolean rowSelection;
	private CheckBox selectAll;
	private boolean statusShowing;

	public SLGrid(boolean rowSelection) {
		super();
		this.rowSelection = rowSelection;

		initWidget(grid);
		grid.addStyleName(PRIMARY_STYLE);
		grid.insertRow(0);
		grid.getRowFormatter().addStyleName(0, PRIMARY_STYLE + "-headerrow");

		if (rowSelection) {
			selectAll = new CheckBox();
			selectAll.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					setSelectAll(selectAll.isChecked());
				}

			});

			grid.setWidget(0, 0, selectAll);
			grid.getCellFormatter().addStyleName(0, 0,
					PRIMARY_STYLE + "-header");
		}
	}

	public int getColumnCount() {
		return grid.getCellCount(0) - getColumnOffset();
	}

	public void setColumn(int column, String text, String width) {
		column += getColumnOffset();

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

		if (rowSelection) {
			grid.setWidget(rowIndex, 0, new CheckBox());
		}
		grid.getRowFormatter().addStyleName(rowIndex,
				PRIMARY_STYLE + "-itemrow");
		for (int i = 0; i < grid.getCellCount(0); i++) {
			grid.getCellFormatter().addStyleName(rowIndex, i,
					PRIMARY_STYLE + "-item");
		}
		return rowIndex - getRowOffset();
	}

	public void removeRow(int row) {
		grid.removeRow(getRowOffset() + row);
	}

	public void removeRows() {
		final int rowOffset = getRowOffset();
		while (grid.getRowCount() > rowOffset) {
			grid.removeRow(rowOffset);
		}
	}

	public Widget getWidget(int row, int column) {
		return grid.getWidget(row + getRowOffset(), column + getColumnOffset());
	}

	public void setWidget(int row, int column, Widget widget) {
		grid
				.setWidget(row + getRowOffset(), column + getColumnOffset(),
						widget);
	}

	public String getText(int row, int column) {
		return grid.getText(row + getRowOffset(), column + getColumnOffset());
	}

	public void setText(int row, int column, String text) {
		grid.setText(row + getRowOffset(), column + getColumnOffset(), text);
	}

	public void clearRow(int row) {
		setSelected(row, false);
		int colCount = grid.getCellCount(0);
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

	public void setStatus(Widget widget) {
		if (!statusShowing) {
			grid.insertRow(1);
			grid.getRowFormatter().addStyleName(1, PRIMARY_STYLE + "-itemrow");
			grid.getFlexCellFormatter().setColSpan(1, getColumnOffset(),
					getColumnCount());
			statusShowing = true;
		}

		grid.setWidget(1, getColumnOffset(), widget);
	}

	public void setStatus(String type, String text) {
		Label textLabel = new Label(text, true);
		textLabel.addStyleName(PRIMARY_STYLE + "-" + type);
		setStatus(textLabel);
	}

	public void setWaitStatus() {
		setStatus(ImageHelper.getWaitImage(16));
	}

	public void clearStatus() {
		if (statusShowing) {
			grid.removeRow(1);
			statusShowing = false;
		}
	}

	private int getColumnOffset() {
		return rowSelection ? 1 : 0;
	}

	private int getRowOffset() {
		return 1 + (statusShowing ? 1 : 0);
	}
}
