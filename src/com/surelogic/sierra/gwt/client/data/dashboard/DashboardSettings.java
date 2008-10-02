package com.surelogic.sierra.gwt.client.data.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.surelogic.sierra.gwt.client.ui.type.Direction;

public class DashboardSettings implements Serializable {
	private static final long serialVersionUID = -54036448093951530L;
	private List<DashboardRow> rows;

	public List<DashboardRow> getRows() {
		if (rows == null) {
			rows = new ArrayList<DashboardRow>();
		}
		return rows;
	}

	public DashboardRow getLastRow() {
		final List<DashboardRow> rows = getRows();
		if (rows.size() == 0) {
			addRow();
		}
		return rows.get(rows.size() - 1);
	}

	public DashboardRow addRow() {
		final DashboardRow newRow = new DashboardRow();
		getRows().add(newRow);
		return newRow;
	}

	public DashboardRow addRow(final int index) {
		final DashboardRow newRow = new DashboardRow();
		getRows().add(index, newRow);
		return newRow;
	}

	public DashboardRow findRow(final DashboardWidget widget) {
		for (final DashboardRow row : getRows()) {
			if (row.getColumns().indexOf(widget) != -1) {
				return row;
			}
		}
		return null;
	}

	public void addColumn(final DashboardWidget widget) {
		getLastRow().getColumns().add(widget);
	}

	public void setColumn(final int col, final DashboardWidget widget) {
		getLastRow().setColumn(col, widget);
	}

	public boolean moveColumn(final DashboardWidget widget,
			final Direction direction) {
		final DashboardRow row = findRow(widget);
		if (row != null) {
			final int rowIndex = getRows().indexOf(row);
			final List<DashboardWidget> columns = row.getColumns();
			final int columnIndex = columns.indexOf(widget);

			if (direction == Direction.LEFT) {
				if (columnIndex > 0 && columns.size() > 1) {
					Collections.swap(columns, columnIndex, columnIndex - 1);
					return true;
				}
			} else if (direction == Direction.RIGHT) {
				if (columnIndex == 0 && columns.size() > 1) {
					Collections.swap(columns, columnIndex, columnIndex + 1);
					return true;
				}
			} else if (direction == Direction.UP) {
				final DashboardRow previousRow = rowIndex == 0 ? null
						: getRows().get(rowIndex - 1);

				if (previousRow == null) {
					if (columns.size() == 1) {
						return false;
					} else {
						final DashboardRow newRow = addRow(0);
						newRow.setColumn(0, widget);
						row.setColumn(columnIndex, null);
						return true;
					}
				}

				int searchIndex = rowIndex - 1;
				boolean found = false;
				List<DashboardWidget> searchCols = null;
				while (searchIndex >= 0 && !found) {
					searchCols = rows.get(searchIndex).getColumns();
					if (searchCols.size() == 1
							|| searchCols.get(columnIndex) != null) {
						found = true;
					} else {
						searchIndex--;
					}
				}
				if (searchIndex < 0) {
					searchIndex = 0;
					searchCols = null;
				}
				final DashboardRow newRow = addRow(searchIndex);
				if (searchCols != null && searchCols.size() == 1) {
					newRow.setColumn(0, widget);
				} else {
					newRow.setColumn(columnIndex, widget);
				}
				row.setColumn(columnIndex, null);
				return true;
			} else if (direction == Direction.DOWN) {

			}
		}
		return false;
	}

	public void removeColumn(final DashboardWidget widget) {
		final DashboardRow row = findRow(widget);
		if (row != null) {
			row.getColumns().remove(widget);
			cleanup();
		}
	}

	public void cleanup() {
		int rowIndex = 0;
		final List<DashboardRow> rows = getRows();
		while (rowIndex < rows.size()) {
			boolean emptyRow = true;
			for (final DashboardWidget w : rows.get(rowIndex).getColumns()) {
				if (w != null) {
					emptyRow = false;
				}
			}
			if (emptyRow) {
				rows.remove(rowIndex);
			} else {
				rowIndex++;
			}
		}
	}

	public static class DashboardRow implements Serializable {
		private static final long serialVersionUID = 5839295537032841813L;

		private List<DashboardWidget> columns;

		public List<DashboardWidget> getColumns() {
			if (columns == null) {
				columns = new ArrayList<DashboardWidget>();
			}
			return columns;
		}

		public void setColumn(final int col, final DashboardWidget widget) {
			final List<DashboardWidget> cols = getColumns();
			while (cols.size() <= col) {
				cols.add(null);
			}
			cols.set(col, widget);
		}

	}

}
