package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.views.FindingsDetailsView;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements
		ISelectionObserver {

	private final int f_addAfterColumn;

	private volatile int f_column = -1;

	private Table f_table = null;

	MListOfFindingsColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn, int addAfterColumn) {
		super(cascadingList, selection, previousColumn);
		f_addAfterColumn = addAfterColumn;
	}

	@Override
	void init() {
		getSelection().setShowing(true);
		getSelection().addObserver(this);
		changed();
	}

	@Override
	void dispose() {
		super.dispose();
		getSelection().setShowing(false);
		getSelection().removeObserver(this);
		if (f_column != -1)
			getCascadingList().emptyFrom(f_column);
	}

	public void selectionChanged(Selection selecton) {
		changed();
	}

	public void selectionStructureChanged(Selection selection) {
		// nothing to do
	}

	private void changed() {
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (f_table != null && f_table.isDisposed()) {
					getSelection().removeObserver(MListOfFindingsColumn.this);
					return;
				}
				getSelection().getManager().getExecutor().execute(
						new Runnable() {
							public void run() {
								refreshData();
								refreshDisplay();
							}
						});
			}
		});
	}

	private static class FindingData {
		String f_summary;
		Importance f_importance;
		long f_findingId;

		String f_projectName;
		String f_packageName;
		int f_lineNumber;
		String f_typeName;

		@Override
		public String toString() {
			return "finding_id=" + f_findingId + " [" + f_importance + "] \""
					+ f_summary + "\" in " + f_projectName + " "
					+ f_packageName + "." + f_typeName + " at line "
					+ f_lineNumber;
		}
	}

	private final LinkedList<FindingData> f_rows = new LinkedList<FindingData>();

	public void refreshData() {
		final String query = getQuery();
		try {
			final Connection c = Data.getConnection();
			try {
				final Statement st = c.createStatement();
				try {
					// System.out.println(query);
					final ResultSet rs = st.executeQuery(query);
					f_rows.clear();
					while (rs.next()) {
						FindingData data = new FindingData();
						data.f_summary = rs.getString(1);
						data.f_importance = Importance.valueOf(rs.getString(2)
								.toUpperCase());
						data.f_findingId = rs.getLong(3);
						data.f_projectName = rs.getString(4);
						data.f_packageName = rs.getString(5);
						data.f_typeName = rs.getString(6);
						data.f_lineNumber = rs.getInt(7);
						f_rows.add(data);
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Query failed to read selected findings", e);
		}
	}

	public String getQuery() {
		StringBuilder b = new StringBuilder();
		b.append("select SUMMARY, IMPORTANCE, FINDING_ID,");
		b.append(" PROJECT, PACKAGE, CLASS, LINE_OF_CODE ");
		getSelection().addWhereClauseTo(b);
		b.append(" order by SUMMARY, IMPORTANCE");
		return b.toString();
	}

	private final Listener f_doubleClick = new Listener() {
		public void handleEvent(Event event) {
			TableItem[] items = f_table.getSelection();
			if (items.length > 0) {
				final FindingData data = (FindingData) items[0].getData();
				JDTUtility.tryToOpenInEditor(data.f_projectName,
						data.f_packageName, data.f_typeName, data.f_lineNumber);
			}
		}
	};

	private final Listener f_singleClick = new Listener() {
		public void handleEvent(Event event) {
			TableItem[] items = f_table.getSelection();
			if (items.length > 0) {
				final FindingData data = (FindingData) items[0].getData();
				final FindingsDetailsView view = (FindingsDetailsView) ViewUtility
						.showView("com.surelogic.sierra.client.eclipse.views.FindingsDetailsView");
				view.findingSelected(data.f_findingId);
			}
		}
	};

	private final IColumn f_iColumn = new IColumn() {
		public Composite createContents(Composite panel) {
			f_table = new Table(panel, SWT.FULL_SELECTION);
			f_table.setLinesVisible(true);
			f_table.addListener(SWT.MouseDoubleClick, f_doubleClick);
			f_table.addListener(SWT.Selection, f_singleClick);

			updateTableContents();
			return f_table;
		}
	};

	private void updateTableContents() {
		if (f_table.isDisposed())
			return;
		f_table.setRedraw(false);
		for (TableItem i : f_table.getItems())
			i.dispose();

		for (FindingData data : f_rows) {
			final TableItem item = new TableItem(f_table, SWT.NONE);
			item.setText(data.f_summary);
			item.setImage(Utility.getImageFor(data.f_importance));
			item.setData(data);
		}
		for (TableColumn c : f_table.getColumns()) {
			c.pack();
		}
		f_table.setRedraw(true);
	}

	private void refreshDisplay() {
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (f_table == null) {
					// create the display table
					f_column = getCascadingList().addColumnAfter(f_iColumn,
							f_addAfterColumn, false);
				} else {
					// update the table's contents
					updateTableContents();
				}
			}
		});
	}
}
