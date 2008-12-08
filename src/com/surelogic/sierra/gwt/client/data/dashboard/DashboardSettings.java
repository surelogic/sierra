package com.surelogic.sierra.gwt.client.data.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.client.ui.type.Direction;
import com.surelogic.sierra.gwt.client.util.LangUtil;

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

	public DashboardRow findWidgetRow(final DashboardWidget widget) {
		for (final DashboardRow row : getRows()) {
			if (row.hasColumn(widget)) {
				return row;
			}
		}
		return null;
	}

	public boolean moveWidget(final DashboardWidget widget,
			final Direction direction) {
		final List<DashboardRow> rows = getRows();
		final DashboardRow row = findWidgetRow(widget);
		if (row != null) {
			final int rowIndex = rows.indexOf(row);

			if (direction == Direction.LEFT) {
				if (!row.isSingleColumn()) {
					row.swapColumns();
					return true;
				}
			} else if (direction == Direction.RIGHT) {
				if (!row.isSingleColumn()) {
					row.swapColumns();
					return true;
				}
			} else if (direction == Direction.UP) {
				final DashboardRow previousRow = rowIndex == 0 ? null : rows
						.get(rowIndex - 1);

				// if we're moving a row with a single column
				if (row.isSingleColumn()) {
					if (previousRow == null) {
						return false;
					}
					rows.remove(row);
					rows.add(rowIndex - 1, row);
					return true;
				}

				// we're moving a column out of a two column row
				final int colIndex = row.indexOf(widget);

				// if there are no rows above our row, or the previous row
				// already has something in our column. Make a new row.
				if (previousRow == null || previousRow.isSingleColumn()
						|| previousRow.getColumn(colIndex) != null) {
					final int newIndex = previousRow == null ? 0 : rowIndex - 1;
					final DashboardRow newRow = new DashboardRow();
					rows.add(newIndex, newRow);
					newRow.moveColumn(row, widget);
					return true;
				}

				// the previous row has space for us, use it
				previousRow.moveColumn(row, widget);
				return true;
			} else if (direction == Direction.DOWN) {
				final DashboardRow nextRow = rowIndex < rows.size() - 1 ? rows
						.get(rowIndex + 1) : null;

				// if we're moving a row with a single column
				if (row.isSingleColumn()) {
					if (nextRow == null) {
						return false;
					}
					addRow(rowIndex + 2, row);
					rows.remove(row);
					return true;
				}

				// we're moving a column out of a two column row
				final int colIndex = row.indexOf(widget);

				// if there are no rows below our row, or the next row already
				// has something in our column. Make a new row.
				if (nextRow == null || nextRow.isSingleColumn()
						|| nextRow.getColumn(colIndex) != null) {
					final DashboardRow newRow = new DashboardRow();
					addRow(rowIndex + 2, newRow);
					newRow.moveColumn(row, widget);
					return true;
				}

				// the next row has space for us, use it
				nextRow.moveColumn(row, widget);
				return true;
			}
		}
		return false;
	}

	private void addRow(final int rowIndex, final DashboardRow row) {
		final List<DashboardRow> rows = getRows();
		if (rowIndex + 2 < rows.size()) {
			rows.add(rowIndex + 2, row);
		} else {
			rows.add(row);
		}
	}

	public void removeWidget(final DashboardWidget widget) {
		final DashboardRow row = findWidgetRow(widget);
		if (row != null) {
			row.removeColumn(widget);
		}
	}

	public void cleanup() {
		int rowIndex = 0;
		final List<DashboardRow> rows = getRows();
		while (rowIndex < rows.size()) {
			if (rows.get(rowIndex).isEmpty()) {
				rows.remove(rowIndex);
			} else {
				rowIndex++;
			}
		}
	}

	public static class DashboardRow implements Serializable {
		private static final long serialVersionUID = 5839295537032841813L;
		private DashboardWidget leftColumn;
		private DashboardWidget rightColumn;
		private DashboardWidget singleColumn;

		public boolean isEmpty() {
			return singleColumn == null && leftColumn == null
					&& rightColumn == null;
		}

		public boolean hasColumn(final DashboardWidget column) {
			return LangUtil.equals(column, singleColumn, leftColumn,
					rightColumn);
		}

		public int indexOf(final DashboardWidget column) {
			if (LangUtil.equals(singleColumn, column)) {
				return 0;
			}
			if (LangUtil.equals(leftColumn, column)) {
				return 1;
			}
			if (LangUtil.equals(rightColumn, column)) {
				return 2;
			}
			return -1;
		}

		public DashboardWidget getColumn(final int index) {
			switch (index) {
			case 0:
				return singleColumn;
			case 1:
				return leftColumn;
			case 2:
				return rightColumn;
			}
			return null;
		}

		public void swapColumns() {
			final DashboardWidget tmp = leftColumn;
			leftColumn = rightColumn;
			rightColumn = tmp;
		}

		public void moveColumn(final DashboardRow oldRow,
				final DashboardWidget widget) {
			singleColumn = null;
			leftColumn = null;
			rightColumn = null;
			if (oldRow.isSingleColumn()) {
				oldRow.setSingleColumn(null);
				singleColumn = widget;
			} else {
				final boolean isLeftColumn = LangUtil.equals(widget, oldRow
						.getLeftColumn());
				if (isLeftColumn) {
					oldRow.setLeftColumn(null);
					leftColumn = widget;
				} else {
					oldRow.setRightColumn(null);
					rightColumn = widget;
				}
			}
		}

		public void removeColumn(final DashboardWidget widget) {
			if (LangUtil.equals(widget, singleColumn)) {
				singleColumn = null;
			} else if (LangUtil.equals(widget, leftColumn)) {
				leftColumn = null;
			} else if (LangUtil.equals(widget, rightColumn)) {
				rightColumn = null;
			}
		}

		public boolean isSingleColumn() {
			return singleColumn != null;
		}

		public DashboardWidget getLeftColumn() {
			return leftColumn;
		}

		public void setLeftColumn(final DashboardWidget leftColumn) {
			this.leftColumn = leftColumn;
		}

		public DashboardWidget getRightColumn() {
			return rightColumn;
		}

		public void setRightColumn(final DashboardWidget rightColumn) {
			this.rightColumn = rightColumn;
		}

		public DashboardWidget getSingleColumn() {
			return singleColumn;
		}

		public void setSingleColumn(final DashboardWidget singleColumn) {
			this.singleColumn = singleColumn;
		}

	}

}
