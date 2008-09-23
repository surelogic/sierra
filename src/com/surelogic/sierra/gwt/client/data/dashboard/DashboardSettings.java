package com.surelogic.sierra.gwt.client.data.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public void addRow() {
		getRows().add(new DashboardRow());
	}

	public void addColumn(final DashboardWidget widget) {
		getLastRow().getColumns().add(widget);
	}

	public void setColumn(final int col, final DashboardWidget widget) {
		final List<DashboardWidget> cols = getLastRow().getColumns();
		while (cols.size() <= col) {
			cols.add(null);
		}
		cols.set(col, widget);
	}

	public void removeColumn(final DashboardWidget dashboardWidget) {
		for (final DashboardRow row : getRows()) {
			row.getColumns().remove(dashboardWidget);
		}
		cleanup();
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

	}

}
