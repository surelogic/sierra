package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.PlatformUI;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.eclipse.job.DatabaseJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.FindingSearch;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsMediator;
import com.surelogic.sierra.client.eclipse.views.FindingsDetailsView;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements
		ISelectionObserver {

	private final int f_addAfterColumn;

	private volatile int f_column = -1;

	private Table f_table = null;

	MListOfFindingsColumn(CascadingList cascadingList, FindingSearch selection,
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
	void initOfNextColumnComplete() {
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MListOfFindingsColumn.super.initOfNextColumnComplete();
			}
		});
	}

	@Override
	void dispose() {
		super.dispose();
		getSelection().setShowing(false);
		getSelection().removeObserver(this);
		if (f_column != -1)
			getCascadingList().emptyFrom(f_column);
	}

	public void selectionChanged(FindingSearch selecton) {
		changed();
	}

	public void selectionStructureChanged(FindingSearch selection) {
		// nothing to do
	}

	private void changed() {
		getCascadingList().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (f_table != null && f_table.isDisposed()) {
					getSelection().removeObserver(MListOfFindingsColumn.this);
					return;
				}
				final Job job = new DatabaseJob("Refresh list of findings") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							refreshData();
							refreshDisplay();
						} catch (Exception e) {
							return SLStatus.createErrorStatus(e);
						} finally {
							initOfNextColumnComplete();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
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

	private final List<FindingData> f_rows = new CopyOnWriteArrayList<FindingData>();

	public void refreshData() {
		final String query = getQuery();
		try {
			final Connection c = Data.readOnlyConnection();
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
		/*
		 * Fix to bug 1115 (an XP specific problem) where the table was redrawn
		 * with lines through the row text. Aaron Silinskas found that a second
		 * call seemed to fix the problem (with a bit of flicker).
		 */
		if (SystemUtils.IS_OS_WINDOWS_XP)
			f_table.setRedraw(true);
	}

	private void setupMenu(final Menu menu) {
		final MenuItem set = new MenuItem(menu, SWT.CASCADE);
		set.setText("Set Importance");
		set.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_DIAMOND_ORANGE));

		/*
		 * Quick audit
		 */

		final MenuItem quickAudit = new MenuItem(menu, SWT.PUSH);
		quickAudit.setText("Mark As Examined by Me");
		quickAudit.setImage(SLImages.getImage(SLImages.IMG_SIERRA_STAMP_SMALL));

		new MenuItem(menu, SWT.SEPARATOR);

		final Menu importanceMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		set.setMenu(importanceMenu);
		final MenuItem setCritical = new MenuItem(importanceMenu, SWT.PUSH);
		setCritical.setText(Importance.CRITICAL.toStringSentenceCase());
		setCritical.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		final MenuItem setHigh = new MenuItem(importanceMenu, SWT.PUSH);
		setHigh.setText(Importance.HIGH.toStringSentenceCase());
		setHigh.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		final MenuItem setMedium = new MenuItem(importanceMenu, SWT.PUSH);
		setMedium.setText(Importance.MEDIUM.toStringSentenceCase());
		setMedium.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		final MenuItem setLow = new MenuItem(importanceMenu, SWT.PUSH);
		setLow.setText(Importance.LOW.toStringSentenceCase());
		setLow.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		final MenuItem setIrrelevant = new MenuItem(importanceMenu, SWT.PUSH);
		setIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
		setIrrelevant.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));

		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = f_table.getSelection();
				final boolean findingSelected = items.length > 0;
				set.setEnabled(findingSelected);
				quickAudit.setEnabled(findingSelected);
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
					quickAudit.setData(data);
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

		quickAudit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (event.widget.getData() instanceof FindingData) {
						final FindingData data = (FindingData) item.getData();
						FindingMutationUtility.asyncComment(data.f_findingId,
								FindingDetailsMediator.STAMP_COMMENT);
					}
				}
			}
		});

		/*
		 * Change the importance of all shown findings
		 */
		final MenuItem setAll = new MenuItem(menu, SWT.CASCADE);
		setAll.setText("Set Importance of All");
		setAll
				.setImage(SLImages
						.getImage(SLImages.IMG_ASTERISK_DIAMOND_ORANGE));

		final Menu importanceAllMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		setAll.setMenu(importanceAllMenu);
		final MenuItem setAllCritical = new MenuItem(importanceAllMenu,
				SWT.PUSH);
		setAllCritical.setText(Importance.CRITICAL.toStringSentenceCase());
		setAllCritical.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_100));
		final MenuItem setAllHigh = new MenuItem(importanceAllMenu, SWT.PUSH);
		setAllHigh.setText(Importance.HIGH.toStringSentenceCase());
		setAllHigh.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_75));
		final MenuItem setAllMedium = new MenuItem(importanceAllMenu, SWT.PUSH);
		setAllMedium.setText(Importance.MEDIUM.toStringSentenceCase());
		setAllMedium.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_50));
		final MenuItem setAllLow = new MenuItem(importanceAllMenu, SWT.PUSH);
		setAllLow.setText(Importance.LOW.toStringSentenceCase());
		setAllLow.setImage(SLImages.getImage(SLImages.IMG_ASTERISK_ORANGE_25));
		final MenuItem setAllIrrelevant = new MenuItem(importanceAllMenu,
				SWT.PUSH);
		setAllIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
		setAllIrrelevant.setImage(SLImages
				.getImage(SLImages.IMG_ASTERISK_ORANGE_0));

		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = f_table.getItems();
				final boolean findingsExist = items.length > 0;
				setAll.setEnabled(findingsExist);
				if (items.length > 0) {
					final Set<Long> finding_ids = new HashSet<Long>();
					for (TableItem item : items) {
						final FindingData data = (FindingData) item.getData();
						finding_ids.add(data.f_findingId);
					}
					setAllCritical.setData(finding_ids);
					setAllHigh.setData(finding_ids);
					setAllMedium.setData(finding_ids);
					setAllLow.setData(finding_ids);
					setAllIrrelevant.setData(finding_ids);
				}
			}
		});

		final Listener f_changeAllImportance = new Listener() {
			@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (event.widget.getData() instanceof Set) {
						final Set<Long> finding_ids = (Set<Long>) event.widget
								.getData();
						final Importance to = Importance.valueOf(item.getText()
								.toUpperCase());
						boolean makeChange = true;
						if (finding_ids.size() > 1) {
							final String msg = "Are you sure you want to "
									+ "change the importance of "
									+ finding_ids.size() + " findings to "
									+ to.toStringSentenceCase();
							if (!MessageDialog.openConfirm(PlatformUI
									.getWorkbench().getDisplay()
									.getActiveShell(),
									"Confirm Multiple Finding Change", msg)) {
								makeChange = false;
							}
						}
						if (makeChange)
							FindingMutationUtility.asyncChangeImportance(
									finding_ids, to);
					}
				}
			}
		};
		setAllCritical.addListener(SWT.Selection, f_changeAllImportance);
		setAllHigh.addListener(SWT.Selection, f_changeAllImportance);
		setAllMedium.addListener(SWT.Selection, f_changeAllImportance);
		setAllLow.addListener(SWT.Selection, f_changeAllImportance);
		setAllIrrelevant.addListener(SWT.Selection, f_changeAllImportance);

		final MenuItem export = new MenuItem(menu, SWT.PUSH);
		export.setText("Export...");
		export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		export.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = f_table.getItems();
				final boolean findingsExist = items.length > 0;
				setAll.setEnabled(findingsExist);
				if (items.length > 0) {
					final Set<Long> finding_ids = new HashSet<Long>();
					for (TableItem item : items) {
						final FindingData data = (FindingData) item.getData();
						finding_ids.add(data.f_findingId);
					}
					final ExportFindingSetDialog dialog = new ExportFindingSetDialog(
							PlatformUI.getWorkbench().getDisplay()
									.getActiveShell(), finding_ids);
					dialog.open();
				}
			}
		});
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
