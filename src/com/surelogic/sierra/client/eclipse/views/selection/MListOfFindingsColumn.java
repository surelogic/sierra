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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
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
		b.append(" order by case");
		b.append(" when IMPORTANCE='Irrelevant' then 5");
		b.append(" when IMPORTANCE=       'Low' then 4");
		b.append(" when IMPORTANCE=    'Medium' then 3");
		b.append(" when IMPORTANCE=      'High' then 2");
		b.append(" when IMPORTANCE=  'Critical' then 1 end, SUMMARY");
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

	/**
	 * Remembers the last finding id that was selected in the list. If the list
	 * is refreshed than an attempt is made to select that finding id again. A
	 * value of <code>-1</code> indicates that no finding is selected.
	 */
	private long f_findingId = -1;

	private final Listener f_singleClick = new Listener() {
		public void handleEvent(Event event) {
			TableItem[] items = f_table.getSelection();
			if (items.length > 0) {
				final FindingData data = (FindingData) items[0].getData();
				/*
				 * Ensure the view is visible but don't change the focus.
				 */
				final FindingsDetailsView view = (FindingsDetailsView) ViewUtility
						.showView(FindingsDetailsView.class.getName(), null,
								IWorkbenchPage.VIEW_VISIBLE);
				f_findingId = data.f_findingId;
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

			final Menu menu = new Menu(f_table.getShell(), SWT.POP_UP);
			f_table.setMenu(menu);

			setupMenu(menu);

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

		boolean selectionFound = false;
		for (FindingData data : f_rows) {
			final TableItem item = new TableItem(f_table, SWT.NONE);
			item.setText(data.f_summary);
			item.setImage(Utility.getImageFor(data.f_importance));
			item.setData(data);
			if (data.f_findingId == f_findingId) {
				selectionFound = true;
				f_table.setSelection(item);
			}
		}
		if (!selectionFound)
			f_findingId = -1;
		for (TableColumn c : f_table.getColumns()) {
			c.pack();
		}
		f_table.setRedraw(true);
	}

	private void setupMenu(final Menu menu) {
		final MenuItem set = new MenuItem(menu, SWT.CASCADE);
		set.setText("Set Importance");
		set.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_DIAMOND_ORANGE));

		final Menu importanceMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		set.setMenu(importanceMenu);
		final MenuItem setCritical = new MenuItem(importanceMenu, SWT.CASCADE);
		setCritical.setText(Importance.CRITICAL.toStringSentenceCase());
		setCritical.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		final MenuItem setHigh = new MenuItem(importanceMenu, SWT.CASCADE);
		setHigh.setText(Importance.HIGH.toStringSentenceCase());
		setHigh.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		final MenuItem setMedium = new MenuItem(importanceMenu, SWT.CASCADE);
		setMedium.setText(Importance.MEDIUM.toStringSentenceCase());
		setMedium.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		final MenuItem setLow = new MenuItem(importanceMenu, SWT.CASCADE);
		setLow.setText(Importance.LOW.toStringSentenceCase());
		setLow.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		final MenuItem setIrrelevant = new MenuItem(importanceMenu, SWT.CASCADE);
		setIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
		setIrrelevant.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));

		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = f_table.getSelection();
				final boolean findingSelected = items.length > 0;
				set.setEnabled(findingSelected);
				if (items.length > 0) {
					final FindingData data = (FindingData) items[0].getData();
					final String currentImportance = data.f_importance
							.toStringSentenceCase();
					setCritical.setData(data);
					setHigh.setData(data);
					setMedium.setData(data);
					setLow.setData(data);
					setIrrelevant.setData(data);
					setCritical.setEnabled(!currentImportance
							.equals(setCritical.getText()));
					setHigh.setEnabled(!currentImportance.equals(setHigh
							.getText()));
					setMedium.setEnabled(!currentImportance.equals(setMedium
							.getText()));
					setLow.setEnabled(!currentImportance.equals(setLow
							.getText()));
					setIrrelevant.setEnabled(!currentImportance
							.equals(setIrrelevant.getText()));
				}
			}
		});

		final Listener f_changeImportance = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (event.widget.getData() instanceof FindingData) {
						final FindingData data = (FindingData) item.getData();
						final Importance to = Importance.valueOf(item.getText()
								.toUpperCase());
						FindingMutationUtility.asyncChangeImportance(
								data.f_findingId, data.f_importance, to);
					}
				}
			}
		};
		setCritical.addListener(SWT.Selection, f_changeImportance);
		setHigh.addListener(SWT.Selection, f_changeImportance);
		setMedium.addListener(SWT.Selection, f_changeImportance);
		setLow.addListener(SWT.Selection, f_changeImportance);
		setIrrelevant.addListener(SWT.Selection, f_changeImportance);
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
