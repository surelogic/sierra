package com.surelogic.sierra.gwt.client.data.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
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

	public DashboardRow getRow(final int row, final boolean createIfNotExists) {
		final List<DashboardRow> rows = getRows();
		DashboardRow currentRow = row < rows.size() ? rows.get(row) : null;
		if (currentRow == null && createIfNotExists) {
			while (rows.size() < row + 1) {
				rows.add(new DashboardRow());
			}
			currentRow = rows.get(row);
		}
		return currentRow;
	}

	public void setWidget(final int row, final int column,
			final DashboardWidget widget) {
		getRow(row, true).setColumn(column, widget);
	}

	public DashboardRow findWidgetRow(final DashboardWidget widget) {
		for (final DashboardRow row : getRows()) {
			if (row.getColumns().indexOf(widget) != -1) {
				return row;
			}
		}
		return null;
	}

	public boolean moveWidget(final DashboardWidget widget,
			final Direction direction) {
		final DashboardRow row = findWidgetRow(widget);
		if (row != null) {
			final int rowIndex = getRows().indexOf(row);
			final List<DashboardWidget> columns = row.getColumns();
			final int columnIndex = columns.indexOf(widget);

			if (direction == Direction.LEFT) {
				if (columnIndex > 0 && columns.size() > 1) {
					row.getColumns().set(columnIndex, null);
					insertColumn(widget, 0, rowIndex, 1);
					return true;
				}
			} else if (direction == Direction.RIGHT) {
				if (columnIndex == 0 && columns.size() > 1) {
					row.getColumns().set(columnIndex, null);
					insertColumn(widget, 1, rowIndex, 1);
					return true;
				}
			} else if (direction == Direction.UP) {
				final DashboardRow previousRow = rowIndex == 0 ? null
						: getRows().get(rowIndex - 1);

				if (previousRow == null) {
					if (columns.size() == 1) {
						return false;
					} else {
						final DashboardRow newRow = new DashboardRow();
						getRows().add(0, newRow);
						newRow.setColumn(columnIndex, widget);
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

				final DashboardRow newRow = new DashboardRow();
				getRows().add(searchIndex, newRow);
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

	private void insertColumn(final DashboardWidget widget,
			final int columnIndex, final int startRow, final int rowIncrement) {
		final List<DashboardRow> rows = getRows();
		final int rowCount = rows.size();

		// move up or down through the rows
		for (int rowIndex = startRow; (rowIndex < rowCount && rowIndex >= 0); rowIndex += rowIncrement) {
			final DashboardRow row = rows.get(rowIndex);
			final List<DashboardWidget> cols = row.getColumns();
			final int colCount = cols.size();

			// if we hit a row that extends across both columns, place our
			// column before it
			if (colCount < 2) {
				final DashboardRow newRow = new DashboardRow();
				if (rowIncrement > 0) {
					rows.add(rowIndex, newRow);
				} else {
					rows.add(rowIndex + 1, newRow);
				}
				newRow.setColumn(columnIndex, widget);
				return;
			} else {
				// the current row has multiple columns, see if there our column
				// is null
				final boolean colIsEmpty = columnIndex < colCount ? cols
						.get(columnIndex) == null : true;
				if (colIsEmpty) {
					// our column is null, place the widget there
					row.setColumn(columnIndex, widget);
					return;
				}
			}
		}

		// we've hit the top or bottom of the dashboard, just insert a new row
		final DashboardRow newRow = new DashboardRow();
		newRow.setColumn(columnIndex, widget);
		if (rowIncrement > 0) {
			rows.add(newRow);
		} else {
			rows.add(0, newRow);
		}

	}

	public void removeWidget(final DashboardWidget widget) {
		final DashboardRow row = findWidgetRow(widget);
		if (row != null) {
			final int columnIndex = row.getColumns().indexOf(widget);
			row.getColumns().set(columnIndex, null);
		}
	}

	public void cleanup() {
		int rowIndex = 0;
		final List<DashboardRow> rows = getRows();
		while (rowIndex < rows.size()) {
			boolean emptyRow = true;
			final List<DashboardWidget> cols = rows.get(rowIndex).getColumns();
			for (final DashboardWidget w : cols) {
				if (w != null) {
					emptyRow = false;
				}
			}
			if (emptyRow) {
				rows.remove(rowIndex);
			} else {
				if (cols.size() > 1) {
					for (int i = 0; i < cols.size(); i++) {
						if (cols.get(i) == null) {
							collapseColumn(rowIndex, i);
						}
					}
				}
				rowIndex++;
			}
		}
	}

	private void collapseColumn(final int rowIndex, final int columnIndex) {
		final List<DashboardRow> rows = getRows();
		int searchRow = rowIndex + 1;
		DashboardWidget foundColumn = null;
		while (searchRow < rows.size() && foundColumn == null) {
			final List<DashboardWidget> cols = rows.get(searchRow).getColumns();
			if (cols.size() < 2) {
				return;
			}
			foundColumn = columnIndex < cols.size() ? cols.get(columnIndex)
					: null;
			if (foundColumn != null) {
				rows.get(rowIndex).setColumn(columnIndex, foundColumn);
				cols.set(columnIndex, null);
			} else {
				searchRow++;
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
