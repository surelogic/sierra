package com.surelogic.sierra.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;
import com.surelogic.sierra.gwt.client.util.ImageHelper;

// TODO add row selection highlighting and tracking of currently selected row
// TODO don't trigger inplace editors until after a row selection has occurred
// -> this is similar to a double click and will prevent inplace editors from activating too easily
// TODO break non-selectable parts of grid into parent class
// TODO add row lock functionality so row edits waiting for a server response can't be changed again
// TODO also may want to provide an icon with a popup if a cell save fails
public class SelectableGrid extends Composite {
	private static final String PRIMARY_STYLE = "sl-Grid";
	private static final String EDITABLE_STYLE = "clickable";

	private final FlexTable grid = new FlexTable();
	private final List rowData = new ArrayList();
	private final List inplaceEditorFactories = new ArrayList();
	private final List listeners = new ArrayList();
	private boolean rowSelection;
	private CheckBox selectAll;
	private boolean statusShowing;

	public SelectableGrid(boolean rowSelection) {
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
					setAllSelections(selectAll.isChecked());
				}

			});

			grid.setWidget(0, 0, selectAll);
			grid.getCellFormatter().addStyleName(0, 0,
					PRIMARY_STYLE + "-header");
		}

		grid.addTableListener(new TableListener() {

			public void onCellClicked(SourcesTableEvents sender, int gridRow,
					int gridColumn) {
				final int row = fromGridRow(gridRow);
				final int column = fromGridColumn(gridColumn);

				if (gridRow == 0 && column >= 0) {
					fireHeaderClickEvent(column);
				} else if (row >= 0 && column >= 0) {
					fireClickEvent(row, column);

					if (!InplaceEditor.isEditing(row, column)) {
						InplaceEditorFactory editorFactory = getInplaceEditor(column);
						if (editorFactory != null) {
							InplaceEditor editor = editorFactory.createEditor(
									SelectableGrid.this, row, column);
							editor.open();
						}
					}
				}
			}
		});
	}

	public void setColumn(int column, String text, String width) {
		int gridColumn = toGridColumn(column);

		grid.setText(0, gridColumn, text);
		grid.getColumnFormatter().setWidth(gridColumn, width);
		grid.getCellFormatter().addStyleName(0, gridColumn,
				PRIMARY_STYLE + "-header");
	}

	public InplaceEditorFactory getInplaceEditor(int column) {
		if (column < inplaceEditorFactories.size()) {
			return (InplaceEditorFactory) inplaceEditorFactories.get(column);
		}
		return null;
	}

	public void setInplaceEditor(int column, InplaceEditorFactory editor) {
		while (inplaceEditorFactories.size() <= column) {
			inplaceEditorFactories.add(null);
		}
		inplaceEditorFactories.set(column, editor);
	}

	public void addListener(SelectableGridListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SelectableGridListener listener) {
		listeners.remove(listener);
	}

	public int getColumnCount() {
		return fromGridColumn(grid.getCellCount(0));
	}

	public int getRowCount() {
		return fromGridRow(grid.getRowCount());
	}

	public int addRow() {
		final int gridRow = grid.getRowCount();

		if (rowSelection) {
			CheckBox c = new CheckBox();
			c.addClickListener(new ClickListener() {
				public void onClick(Widget sender) {
					fireSelectionEvent(gridRow);
				}
			});
			grid.setWidget(gridRow, 0, c);
		}
		grid.getRowFormatter()
				.addStyleName(gridRow, PRIMARY_STYLE + "-itemrow");
		for (int i = 0; i < grid.getCellCount(0); i++) {
			grid.getCellFormatter().addStyleName(gridRow, i,
					PRIMARY_STYLE + "-item");
		}

		int row = fromGridRow(gridRow);
		if (row < rowData.size()) {
			rowData.set(row, null);
		}
		return row;
	}

	public void removeRow(int row) {
		grid.removeRow(toGridRow(row));
		if (row < rowData.size()) {
			rowData.remove(row);
		}
	}

	public void removeRows() {
		final int rowOffset = getRowOffset();
		while (grid.getRowCount() > rowOffset) {
			grid.removeRow(rowOffset);
		}
		rowData.clear();
	}

	public Widget getWidget(int row, int column) {
		return grid.getWidget(toGridRow(row), toGridColumn(column));
	}

	public void setWidget(int row, int column, Widget widget) {
		grid.setWidget(toGridRow(row), toGridColumn(column), widget);
	}

	public String getText(int row, int column) {
		return grid.getText(toGridRow(row), toGridColumn(column));
	}

	public void setText(int row, int column, String text) {
		grid.setText(toGridRow(row), toGridColumn(column), text);
		if (getInplaceEditor(column) != null) {
			grid.getCellFormatter().addStyleName(toGridRow(row),
					toGridColumn(column), EDITABLE_STYLE);
		} else {
			grid.getCellFormatter().removeStyleName(toGridColumn(row),
					toGridColumn(column), EDITABLE_STYLE);
		}
	}

	public Object getRowData(int row) {
		if (row < rowData.size()) {
			return rowData.get(row);
		}
		return null;
	}

	public void setRowData(int row, Object data) {
		while (row >= rowData.size()) {
			rowData.add(null);
		}
		rowData.set(row, data);
	}

	public void clearRow(int row) {
		setSelected(row, false);
		int gridColCount = grid.getCellCount(0);
		int gridRow = toGridRow(row);
		for (int i = getColumnOffset(); i < gridColCount; i++) {
			grid.setText(gridRow, i, "");
		}
		if (row < rowData.size()) {
			rowData.set(row, null);
		}
	}

	public void setAllSelections(boolean selected) {
		if (rowSelection) {
			((CheckBox) grid.getWidget(0, 0)).setChecked(selected);
			int rowCount = getRowCount();
			for (int i = 0; i < rowCount; i++) {
				setSelected(i, selected);
			}
		}
	}

	public boolean hasSelected() {
		if (rowSelection) {
			int rowCount = getRowCount();
			for (int i = 0; i < rowCount; i++) {
				if (isSelected(i)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSelected(int row) {
		if (rowSelection) {
			CheckBox rowCheckBox = (CheckBox) grid.getWidget(toGridRow(row), 0);
			return rowCheckBox.isChecked();
		}
		return false;
	}

	public void setSelected(int row, boolean selected) {
		if (rowSelection) {
			CheckBox rowCheckBox = (CheckBox) grid.getWidget(toGridRow(row), 0);
			rowCheckBox.setChecked(selected);
		}
		fireSelectionEvent(row);
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

	public void fireSelectionEvent(int row) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			SelectableGridListener listener = (SelectableGridListener) it
					.next();
			listener.onSelect(row, getRowData(row));
		}
	}

	public void fireHeaderClickEvent(int column) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			SelectableGridListener listener = (SelectableGridListener) it
					.next();
			listener.onHeaderClick(grid, column);
		}
	}

	public void fireClickEvent(int row, int column) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			SelectableGridListener listener = (SelectableGridListener) it
					.next();
			listener.onClick(grid, row, column, getRowData(row));
		}
	}

	public Object fireChangeEvent(int row, int column, Object oldValue,
			Object newValue) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			SelectableGridListener listener = (SelectableGridListener) it
					.next();
			newValue = listener.onChange(grid, row, column, oldValue, newValue);
		}
		return newValue;
	}

	private int getColumnOffset() {
		return rowSelection ? 1 : 0;
	}

	private int fromGridColumn(int column) {
		return column - getColumnOffset();
	}

	private int toGridColumn(int column) {
		return column + getColumnOffset();
	}

	private int getRowOffset() {
		return 1 + (statusShowing ? 1 : 0);
	}

	private int fromGridRow(int row) {
		return row - getRowOffset();
	}

	private int toGridRow(int row) {
		return row + getRowOffset();
	}
}
