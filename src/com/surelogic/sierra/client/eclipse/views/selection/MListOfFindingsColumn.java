package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.ViewUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLStatus;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.dialogs.MaximumFindingsShownDialog;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsMediator;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements
		ISelectionObserver {

	private Table f_table = null;

	MListOfFindingsColumn(CascadingList cascadingList, Selection selection,
			MColumn previousColumn) {
		super(cascadingList, selection, previousColumn);
	}

	@Override
	void init() {
		getSelection().setShowingFindings(true);
		getSelection().addObserver(this);
		changed();
	}

	@Override
	void initOfNextColumnComplete() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MListOfFindingsColumn.super.initOfNextColumnComplete();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	void dispose() {
		super.dispose();
		getSelection().setShowingFindings(false);
		getSelection().removeObserver(this);
		final int column = getColumnIndex();
		if (column != -1)
			getCascadingList().emptyFrom(column);
	}

	@Override
	int getColumnIndex() {
		if (f_table.isDisposed())
			return -1;
		else
			return getCascadingList().getColumnIndexOf(f_table);
	}

	public void selectionChanged(Selection selecton) {
		changed();
	}

	private void changed() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_table != null && f_table.isDisposed()) {
					getSelection().removeObserver(MListOfFindingsColumn.this);
				} else {
					final Job job = new DatabaseJob("Refresh list of findings") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								refreshData();
								refreshDisplay();
							} catch (Exception e) {
								final int errNo = 60;
								final String msg = I18N.err(errNo);
								return SLStatus
										.createErrorStatus(errNo, msg, e);
							} finally {
								initOfNextColumnComplete();
							}
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static class FindingData {
		String f_summary;
		Importance f_importance;
		long f_findingId;

		String f_projectName;
		String f_packageName;
		int f_lineNumber;
		String f_typeName;
		String f_findingTypeName;
		String f_categoryName;
		String f_toolName;

		@Override
		public String toString() {
			return "finding_id=" + f_findingId + " [" + f_importance
					+ "] of type " + f_findingTypeName + " and category "
					+ f_categoryName + " \"" + f_summary + "\" in "
					+ f_projectName + " " + f_packageName + "." + f_typeName
					+ " at line " + f_lineNumber + " from " + f_toolName;
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
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"List of findings query: " + query);
					}
					final ResultSet rs = st.executeQuery(query);
					f_rows.clear();
					final int findingsListLimit = PreferenceConstants
							.getFindingsListLimit();
					while (rs.next()) {
						if (f_rows.size() < findingsListLimit) {
							FindingData data = new FindingData();
							data.f_summary = rs.getString(1);
							data.f_importance = Importance.valueOf(rs
									.getString(2).toUpperCase());
							data.f_findingId = rs.getLong(3);
							data.f_projectName = rs.getString(4);
							data.f_packageName = rs.getString(5);
							data.f_typeName = rs.getString(6);
							data.f_lineNumber = rs.getInt(7);
							data.f_findingTypeName = rs.getString(8);
							data.f_categoryName = rs.getString(9);
							data.f_toolName = rs.getString(10);
							f_rows.add(data);
						} else {
							/*
							 * We skipped a row so inform the user
							 */
							class WarningDialog extends SLUIJob {

								final private int f_findingsLimit;
								final private int f_findingsCount;

								public WarningDialog(int findingsLimit,
										int findingsCount) {
									f_findingsLimit = findingsListLimit;
									f_findingsCount = findingsCount;
								}

								@Override
								public IStatus runInUIThread(
										IProgressMonitor monitor) {
									if (PreferenceConstants
											.warnAboutMaximumFindingsShown()) {
										Dialog dialog = new MaximumFindingsShownDialog(
												f_findingsLimit,
												f_findingsCount);
										dialog.open();
									}
									return Status.OK_STATUS;
								}
							}
							final UIJob job = new WarningDialog(
									findingsListLimit, getSelection()
											.getFindingCountPorous());
							job.schedule();
							break;
						}
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

	/**
	 * Generates a query that is used in this class and by the export of
	 * findings. The category and tool are only used by the export.
	 * <p>
	 * The design here is suspect and probably needs some re-work.
	 * 
	 * @return Query on the overview of findings.
	 */
	public String getQuery() {
		StringBuilder b = new StringBuilder();
		b.append("select SUMMARY, IMPORTANCE, FINDING_ID,");
		b.append(" PROJECT, PACKAGE, CLASS, LINE_OF_CODE,");
		b.append(" FINDING_TYPE, CATEGORY, TOOL ");
		getSelection().addWhereClauseTo(b);
		b.append(" order by case");
		b.append(" when IMPORTANCE='Irrelevant' then 5");
		b.append(" when IMPORTANCE=       'Low' then 4");
		b.append(" when IMPORTANCE=    'Medium' then 3");
		b.append(" when IMPORTANCE=      'High' then 2");
		b.append(" when IMPORTANCE=  'Critical' then 1 end, SUMMARY");
		return b.toString();
	}

	private final KeyListener f_keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if (e.character == 0x01 && f_table != null) {
				f_table.selectAll();
			}
		}

		public void keyReleased(KeyEvent e) {
			// Nothing to do
		}
	};

	private final AtomicReference<FindingData> lastSelected = new AtomicReference<FindingData>();

	private final Listener f_doubleClick = new Listener() {
		public void handleEvent(Event event) {
			final FindingData data = lastSelected.get();
			if (data != null) {
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
			TableItem item = (TableItem) event.item;
			if (item != null) {
				final FindingData data = (FindingData) item.getData();
				lastSelected.set(data);

				/*
				 * Ensure the view is visible but don't change the focus.
				 */
				final FindingDetailsView view = (FindingDetailsView) ViewUtility
						.showView(FindingDetailsView.class.getName(), null,
								IWorkbenchPage.VIEW_VISIBLE);
				f_findingId = data.f_findingId;
				view.findingSelected(data.f_findingId);
			}
		}
	};

	private final IColumn f_iColumn = new IColumn() {
		public Composite createContents(Composite panel) {
			f_table = new Table(panel, SWT.FULL_SELECTION | SWT.MULTI
					| SWT.VIRTUAL);
			f_table.setLinesVisible(true);
			f_table.addListener(SWT.MouseDoubleClick, f_doubleClick);
			f_table.addListener(SWT.Selection, f_singleClick);
			f_table.addKeyListener(f_keyListener);
			f_table.setItemCount(0);

			f_table.addListener(SWT.SetData, new Listener() {
			  public void handleEvent(Event event) {
			    final TableItem item = (TableItem) event.item;
			    final int index = event.index;
			    FindingData data = f_rows.get(index);
			    initTableItem(data, item);
			  }
			});
		   
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
		for (TableItem i : f_table.getItems()) {
			if (i != null) {
				i.dispose();
			}
		}
		f_table.setItemCount(f_rows.size());

		boolean selectionFound = false;
		int i = 0;
		for (FindingData data : f_rows) {
			/*
			 * final TableItem item = new TableItem(f_table, SWT.NONE);
			 * selectionFound = initTableItem(data, item);
			 */
			if (data.f_findingId == f_findingId) {
				initTableItem(data, f_table.getItem(i));
				selectionFound = true;
				break;
			}
			i++;
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

	private boolean initTableItem(FindingData data, final TableItem item) {
		item.setText(data.f_summary);
		item.setImage(Utility.getImageFor(data.f_importance));
		item.setData(data);
		if (data.f_findingId == f_findingId) {
			f_table.setSelection(item);
			return true;
		}
		return false;
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

		final MenuItem filterFindingTypeFromScans = new MenuItem(menu, SWT.PUSH);
		filterFindingTypeFromScans
				.setText("Filter Finding Type From Future Scans");

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
				filterFindingTypeFromScans.setEnabled(findingSelected);
				if (items.length > 0) {
					String importanceSoFar = null;
					String findingTypeSoFar = null;
					for (TableItem item : items) {
						final FindingData data = (FindingData) item.getData();
						String importance = data.f_importance
								.toStringSentenceCase();
						if (importanceSoFar == null) {
							importanceSoFar = importance;
						} else if (!importanceSoFar.equals(importance)) {
							importanceSoFar = ""; // More than one
						}
						// Otherwise, it's all the same so far

						String findingType = data.f_findingTypeName;
						if (findingTypeSoFar == null) {
							findingTypeSoFar = findingType;
						} else if (!findingTypeSoFar.equals(findingType)) {
							findingTypeSoFar = ""; // More than one
						}
					}
					final String currentImportance = importanceSoFar;
					final String currentFindingType = findingTypeSoFar;
					setCritical.setData(items);
					setHigh.setData(items);
					setMedium.setData(items);
					setLow.setData(items);
					setIrrelevant.setData(items);
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
					quickAudit.setData(items);
					if ("".equals(currentFindingType)) {
						filterFindingTypeFromScans.setEnabled(false);
						filterFindingTypeFromScans
								.setText("Filter Selected Findings From Future Scans");
					} else {
						filterFindingTypeFromScans.setEnabled(true);
						filterFindingTypeFromScans.setData(items);
						filterFindingTypeFromScans.setText("Filter All '"
								+ currentFindingType
								+ "' Findings From Future Scans");
					}
				}
			}
		});

		final Listener changeImportance = new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (item.getData() instanceof TableItem[]) {
						final TableItem[] items = (TableItem[]) item.getData();
						final Importance to = Importance.valueOf(item.getText()
								.toUpperCase());
						if (items.length == 1) {
							final FindingData data = (FindingData) items[0]
									.getData();
							FindingMutationUtility.asyncChangeImportance(
									data.f_findingId, data.f_importance, to);
						} else {
							final Collection<Long> ids = extractFindingIds(items);
							FindingMutationUtility.asyncChangeImportance(ids,
									to);
						}
					}
				}
			}
		};
		setCritical.addListener(SWT.Selection, changeImportance);
		setHigh.addListener(SWT.Selection, changeImportance);
		setMedium.addListener(SWT.Selection, changeImportance);
		setLow.addListener(SWT.Selection, changeImportance);
		setIrrelevant.addListener(SWT.Selection, changeImportance);

		quickAudit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (item.getData() instanceof TableItem[]) {
						final TableItem[] items = (TableItem[]) item.getData();
						if (items.length == 1) {
							final FindingData data = (FindingData) items[0]
									.getData();
							FindingMutationUtility.asyncComment(
									data.f_findingId,
									FindingDetailsMediator.STAMP_COMMENT);
						} else {
							final Collection<Long> ids = extractFindingIds(items);
							FindingMutationUtility.asyncComment(ids,
									FindingDetailsMediator.STAMP_COMMENT);
						}
					}
				}
			}
		});

		filterFindingTypeFromScans.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					MenuItem item = (MenuItem) event.widget;
					if (item.getData() instanceof TableItem[]) {
						final TableItem[] items = (TableItem[]) item.getData();
						final FindingData data = (FindingData) items[0]
								.getData();
						if (items.length == 1) {
							FindingMutationUtility
									.asyncFilterFindingTypeFromScans(
											data.f_findingId,
											data.f_findingTypeName);
						} else {
							// FIX Assuming that all the finding types are the
							// same?
							final Collection<Long> ids = extractFindingIds(items);
							FindingMutationUtility
									.asyncFilterFindingTypeFromScans(ids,
											data.f_findingTypeName);
						}
					}
				}
			}
		});

		final MenuItem export = new MenuItem(menu, SWT.PUSH);
		export.setText("Export...");
		export.setImage(SLImages.getImage(SLImages.IMG_EXPORT));
		export.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final ExportFindingSetDialog dialog = new ExportFindingSetDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), getQuery());
				dialog.open();
			}
		});
	}

	private void refreshDisplay() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (f_table == null) {
					int addAfterColumn = getPreviousColumn().getColumnIndex();
					// create the display table
					getCascadingList().addColumnAfter(f_iColumn,
							addAfterColumn, false);
				} else {
					// update the table's contents
					updateTableContents();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private static Collection<Long> extractFindingIds(final TableItem[] items) {
		final Collection<Long> ids = new ArrayList<Long>(items.length);
		for (TableItem ti : items) {
			FindingData fd = (FindingData) ti.getData();
			if (fd != null) {
				ids.add(fd.f_findingId);
			}
		}
		return ids;
	}
}
