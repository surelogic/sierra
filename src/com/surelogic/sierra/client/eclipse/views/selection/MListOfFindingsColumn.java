package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.CommonImages;
import com.surelogic.common.StringComparators;
import com.surelogic.common.eclipse.CascadingList;
import com.surelogic.common.eclipse.JDTUtility;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.eclipse.SWTUtility;
import com.surelogic.common.eclipse.CascadingList.IColumn;
import com.surelogic.common.eclipse.jobs.DatabaseJob;
import com.surelogic.common.eclipse.jobs.SLUIJob;
import com.surelogic.common.eclipse.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.jsure.JSureUtil;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.selection.Column;
import com.surelogic.sierra.client.eclipse.model.selection.ColumnSort;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsMediator;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.tool.message.AssuranceType;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements
		ISelectionObserver {
	/**
	 * For more information please see <a href=
	 * "http://publicobject.com/glazedlists/documentation/swt_virtual_tables.html"
	 * >SWT virtual tables</a>.
	 */
	private static final boolean USE_VIRTUAL = true;

	private Table f_table = null;

	MListOfFindingsColumn(final CascadingList cascadingList,
			final Selection selection, final MColumn previousColumn) {
		super(cascadingList, selection, previousColumn);
		f_tables.add(this);
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
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				MListOfFindingsColumn.super.initOfNextColumnComplete();
				boolean limited;
				synchronized (MListOfFindingsColumn.this) {
					limited = f_isLimited;
				}
				notifyObserversOfLimitedFindings(limited);
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
		if (column != -1) {
			getCascadingList().emptyFrom(column);
		}

		notifyObserversOfDispose();
	}

	@Override
	int getColumnIndex() {
		if (f_table.isDisposed()) {
			return -1;
		} else {
			return getCascadingList().getColumnIndexOf(f_table);
		}
	}

	@Override
	public void forceFocus() {
		f_table.forceFocus();
		getCascadingList().show(index);
	}

	public void selectionChanged(final Selection selecton) {
		changed();
	}

	private void changed() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				if (f_table != null && f_table.isDisposed()) {
					getSelection().removeObserver(MListOfFindingsColumn.this);
				} else {
					final long now = startingUpdate();
					final Job job = new DatabaseJob("Refresh list of findings",
							Job.INTERACTIVE) {
						@Override
						protected IStatus run(final IProgressMonitor monitor) {
							boolean keepGoing = false;
							try {
								keepGoing = refreshData(now);
								if (keepGoing) {
									refreshDisplay();
								}
							} catch (final Exception e) {
								final int errNo = 60;
								final String msg = I18N.err(errNo);
								return SLEclipseStatusUtility
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
		String f_findingType;
		String f_findingTypeName;
		String f_toolName;
		AssuranceType f_assuranceType;
		int index; 

		public FindingData(int i) {
			index = i;
		}

		@Override
		public String toString() {
			// FIX f_assuranceType
			return "finding_id=" + f_findingId + " [" + f_importance
					+ "] of type " + f_findingType + " \"" + f_summary
					+ "\" in " + f_projectName + " " + f_packageName + "."
					+ f_typeName + " at line " + f_lineNumber + " from "
					+ f_toolName;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof FindingData) {
				final FindingData other = (FindingData) o;
				return f_findingId == other.f_findingId;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (int) f_findingId;
		}
	}

	private static abstract class ColumnData extends Column implements
			Cloneable, Comparator<FindingData> {
		ColumnData(final String name) {
			super(name);
		}

		ColumnData(final String name, final boolean visible,
				final ColumnSort sort) {
			this(name);
			this.visible = visible;
			this.width = -1;
			this.sort = sort;
		}

		int getAlignment() {
			return SWT.LEFT;
		}

		void setSort(final ColumnSort s) {
			sort = s;
		}

		void setWidth(final int w) {
			width = w;
		}

		void setIndex(final int i) {
			index = i;
		}

		String getText(final FindingData data) {
			return "";
		}

		Image getImage(final FindingData data) {
			return null;
		}

		public int compare(final FindingData o1, final FindingData o2) {
			return sort == ColumnSort.SORT_DOWN ? -compareInternal(o1, o2)
					: compareInternal(o1, o2);
		}

		protected int compareInternal(final FindingData o1, final FindingData o2) {
			return StringComparators.SORT_ALPHABETICALLY.compare(getText(o1),
					getText(o2));
		}

		@Override
		protected ColumnData clone() {
			try {
				return (ColumnData) super.clone();
			} catch (final CloneNotSupportedException e) {
				throw new RuntimeException("Couldn't clone " + this);
			}
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	private static final List<ColumnData> f_columnPrototypes = createColumnPrototypes();

	private static List<ColumnData> createColumnPrototypes() {
		final List<ColumnData> prototypes = new ArrayList<ColumnData>();
		prototypes.add(new ColumnData("Summary", true, ColumnSort.SORT_UP) {
			@Override
			String getText(final FindingData data) {
				return data.f_summary;
			}

			@Override
			Image getImage(final FindingData data) {
				if (data.f_assuranceType != null) {
					return JSureUtil.getImageFor(data.f_findingType,
							data.f_assuranceType);
				}
				return Utility.getImageFor(data.f_importance);
			}
		});
		prototypes.add(new ColumnData("Importance") {
			@Override
			String getText(final FindingData data) {
				return data.f_importance.toStringSentenceCase();
			}

			/*
			 * @Override Image getImage(FindingData data) { return
			 * Utility.getImageFor(data.f_importance); }
			 */
			@Override
			protected int compareInternal(final FindingData o1,
					final FindingData o2) {
				return o1.f_importance.ordinal() - o2.f_importance.ordinal();
			}
		});
		prototypes.add(new ColumnData("Project") {
			@Override
			String getText(final FindingData data) {
				return data.f_projectName;
			}
		});
		prototypes.add(new ColumnData("Package") {
			@Override
			String getText(final FindingData data) {
				return data.f_packageName;
			}
		});
		prototypes.add(new ColumnData("Line#") {
			@Override
			String getText(final FindingData data) {
				return Integer.toString(data.f_lineNumber);
			}

			@Override
			int getAlignment() {
				return SWT.RIGHT;
			}

			@Override
			protected int compareInternal(final FindingData o1,
					final FindingData o2) {
				return o1.f_lineNumber - o2.f_lineNumber;
			}
		});
		prototypes.add(new ColumnData("Type") {
			@Override
			String getText(final FindingData data) {
				return data.f_typeName;
			}
		});
		prototypes.add(new ColumnData("Finding Type") {
			@Override
			String getText(final FindingData data) {
				return data.f_findingTypeName;
			}
		});
		/*
		prototypes.add(new ColumnData("Finding Category") {
			@Override
			String getText(final FindingData data) {
				// FIXME Should we remove this column? Currently doing so throws
				// and IndexOutOfBoundsException
				return "";
			}
		});
		*/
		prototypes.add(new ColumnData("Tool") {
			@Override
			String getText(final FindingData data) {
				return data.f_toolName;
			}
		});

		int i = 0;
		for (final ColumnData data : prototypes) {
			data.setIndex(i);
			i++;
		}
		return Collections.unmodifiableList(prototypes);
	}

	public static Iterable<String> getColumnNames() {
		final List<String> names = new ArrayList<String>();
		for (final ColumnData data : f_columnPrototypes) {
			names.add(data.getName());
		}
		return names;
	}

	private final ReadWriteLock rowsLock = new ReentrantReadWriteLock();
	private final List<FindingData> f_rows = new ArrayList<FindingData>();
	private boolean f_isLimited = false;

	/**
	 * @return true if updating
	 */
	public boolean refreshData(final long now) {
		final String query = getQuery();
		try {
			final Connection c = Data.getInstance().readOnlyConnection();
			try {
				final Statement st = c.createStatement();
				try {
					if (SLLogger.getLogger().isLoggable(Level.FINE)) {
						SLLogger.getLogger().fine(
								"List of findings query: " + query);
					}
					if (!continueUpdate(now)) {
						return false;
					}
					final ResultSet rs = st.executeQuery(query);
					try {
						rowsLock.writeLock().lock();
						f_rows.clear();
						f_isLimited = false;

						final int findingsListLimit = PreferenceConstants
								.getFindingsListLimit();
						int i = 0;
						while (rs.next()) {
							if (i < findingsListLimit) {
								final FindingData data = new FindingData(i);
								data.f_summary = rs.getString(1);
								data.f_importance = Importance.valueOf(rs
										.getString(2).toUpperCase());
								data.f_findingId = rs.getLong(3);
								data.f_projectName = rs.getString(4);
								data.f_packageName = rs.getString(5);
								data.f_typeName = rs.getString(6);
								data.f_lineNumber = rs.getInt(7);
								data.f_findingType = rs.getString(8);
								data.f_findingTypeName = rs.getString(9);
								data.f_toolName = rs.getString(10);

								final String aType = rs.getString(11);
								data.f_assuranceType = AssuranceType
										.fromFlag(aType);
								f_rows.add(data);
								i++;
							} else {
								f_isLimited = true;
								break;
							}
						}
					} finally {
						rowsLock.writeLock().unlock();
					}
				} finally {
					st.close();
				}
			} finally {
				c.close();
			}
		} catch (final SQLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Query failed to read selected findings", e);
		}
		return true;
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
		final StringBuilder b = new StringBuilder();
		final Selection s = getSelection();
		final String query = s.usesJoin() ? "FindingsSelectionView.showJoin"
				: "FindingsSelectionView.show";
		b.append(QB.get(query, getSelection().getWhereClause()));
		return b.toString();
	}

	private final KeyListener f_keyListener = new KeyListener() {
		public void keyPressed(final KeyEvent e) {
			if (e.character == 0x01 && f_table != null) {
				f_table.selectAll();
				e.doit = false; // Handled
			} else if (e.keyCode == SWT.ARROW_LEFT) {
				getPreviousColumn().forceFocus();
				e.doit = false; // Handled
			} else if (e.keyCode == SWT.ARROW_RIGHT || e.character == ' ') {
				f_doubleClick.handleEvent(null);
				e.doit = false; // Handled
			}
		}

		public void keyReleased(final KeyEvent e) {
			// Nothing to do
		}
	};

	private final AtomicReference<FindingData> lastSelected = new AtomicReference<FindingData>();
	private final Stack<FindingData> nearSelected = new Stack<FindingData>();

	private final Listener f_doubleClick = new Listener() {
		public void handleEvent(final Event event) {
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
	private long f_selectedFindingId = -1;

	private final Listener f_singleClick = new Listener() {
		public void handleEvent(final Event event) {
			final TableItem item = (TableItem) event.item;
			if (item != null) {
				final FindingData data = (FindingData) item.getData();
				if (data != null) {
					lastSelected.set(data);
					if (rowsLock.readLock().tryLock()) {
						try {
							addNearSelected(data.index - 1);
							addNearSelected(data.index + 1);
						} finally {
							rowsLock.readLock().unlock();
						}
					}
					/*
					 * Ensure the view is visible but don't change the focus.
					 */
					f_selectedFindingId = data.f_findingId;
					FindingDetailsView.findingSelected(data.f_findingId, false);
				} else {
					LOG.severe("No data for " + item.getText(0));
				}
			}
		}
	};

	protected void addNearSelected(final int i) {
		if (i >= 0 && i < f_rows.size()) {
			nearSelected.add(f_rows.get(i));
		}
	}

	private final IColumn f_iColumn = new IColumn() {
		public Composite createContents(final Composite panel) {
			int mods = SWT.FULL_SELECTION | SWT.MULTI;
			if (USE_VIRTUAL) {
				mods = mods | SWT.VIRTUAL;
			}
			f_table = new Table(panel, mods);
			f_table.setLinesVisible(true);
			f_table.addListener(SWT.MouseDoubleClick, f_doubleClick);
			f_table.addListener(SWT.Selection, f_singleClick);
			f_table.addKeyListener(f_keyListener);
			f_table.setItemCount(0);
			createTableColumns();

			if (USE_VIRTUAL) {
				f_table.addListener(SWT.SetData, new Listener() {
					// Only called the first time the TableItem is shown
					// Intended to initialize the item
					public void handleEvent(final Event event) {
						final TableItem item = (TableItem) event.item;
						final int index = event.index;
						final FindingData data = f_rows.get(index);						
						// 1-indexed
						initTableItem(event.index, data, item);
					}
				});
			}
			f_table.addListener(SWT.Traverse, new Listener() {
				public void handleEvent(final Event e) {
					switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE:
						setCustomTabTraversal(e);
						if (getPreviousColumn() instanceof MRadioMenuColumn) {
							final MRadioMenuColumn column = (MRadioMenuColumn) getPreviousColumn();
							column.escape(null);
							/*
							 * column.clearSelection(); column.emptyAfter(); //
							 * e.g. eliminate myself column.forceFocus();
							 */
						}
						break;
					case SWT.TRAVERSE_TAB_NEXT:
						// Cycle back to the first columns
						setCustomTabTraversal(e);
						getFirstColumn().forceFocus();
						break;
					case SWT.TRAVERSE_TAB_PREVIOUS:
						setCustomTabTraversal(e);
						getPreviousColumn().forceFocus();
						break;
					case SWT.TRAVERSE_RETURN:
						setCustomTabTraversal(e);
						f_doubleClick.handleEvent(null);
						break;
					}
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
		if (f_table.isDisposed()) {
			return;
		}

		if (rowsLock.readLock().tryLock()) {
			try {
				f_table.setRedraw(false);
				
				final Set<FindingData> selected = getSelectedItems(f_table);
				f_table.removeAll();
				sortBasedOnColumns();

				if (USE_VIRTUAL) {
					// Creates that many table items -- not necessarily
					// initialized
					f_table.setItemCount(f_rows.size());
				}

				boolean selectionFound = false;
				int i = 0;
				for (final FindingData data : f_rows) {
					final boolean rowSelected = selected.contains(data) || data.f_findingId == f_selectedFindingId;					
					if (!USE_VIRTUAL) {
						final TableItem item = new TableItem(f_table, SWT.NONE);
						initTableItem(i, data, item);
						selectionFound |= rowSelected;
					}
					// Only needed if virtual
					// Sets up the table to show the previous selection
					else if (rowSelected) {
						selectItem(i, data);
						selectionFound = true;
					}
					i++;
				}
				if (!selectionFound) {
					// Look for a near-selection
					FindingData data = null;
					if (!nearSelected.isEmpty()) {
						final Map<FindingData, FindingData> rows = new HashMap<FindingData, FindingData>(
								f_rows.size());
						for (final FindingData row : f_rows) {
							rows.put(row, row);
						}

						while (!nearSelected.isEmpty()) {
							data = nearSelected.pop();

							final FindingData newData = rows.get(data);
							if (newData != null) {
								selectItem(newData.index, newData);
								selectionFound = true;
								break;
							}
						}
					}
					f_selectedFindingId = (data == null) ? -1 : data.f_findingId;
				}
				nearSelected.clear();

				/*
				 * for (TableColumn c : f_table.getColumns()) { c.pack(); }
				 */
				updateTableColumns();

				// f_table.layout();
				if (USE_VIRTUAL) {
					// Computes the appropriate width for the longest item
					final Point p = f_table.getSize();
					final int width = computeValueWidth();
					f_table.setSize(width, p.y);
				}

				f_table.setRedraw(true);
				/*
				 * Fix to bug 1115 (an XP specific problem) where the table was
				 * redrawn with lines through the row text. Aaron Silinskas
				 * found that a second call seemed to fix the problem (with a
				 * bit of flicker).
				 */
				if (SystemUtils.IS_OS_WINDOWS_XP) {
					f_table.setRedraw(true);
				}
				if (selectionFound) {					
					final UIJob job = new SLUIJob() {
						@Override
						public IStatus runInUIThread(final IProgressMonitor monitor) {
							f_table.showSelection();
							
							// avoid scroll bar position being to the right
							f_table.showColumn(f_table.getColumn(0));
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				}				
			} finally {
				rowsLock.readLock().unlock();
			}
		}
	}

	private void selectItem(int i, FindingData data) {
		if (i != -1) {
			f_table.select(i);
		}
	}

	private static Set<FindingData> getSelectedItems(Table table) {
		if (table.getSelectionCount() == 0) {
			return Collections.emptySet();
		}
		final Set<FindingData> selected = new HashSet<FindingData>();
		for(TableItem item : table.getSelection()) {
			selected.add((FindingData) item.getData());
		}
		return selected;
	}

	/**
	 * Sync'd by updateTableContents()
	 */
	private void sortBasedOnColumns() {
		Comparator<FindingData> c = null;
		// Traverse order backwards to construct proper comparator
		final int[] order = f_table.getColumnOrder();
		for (int i = order.length - 1; i >= 0; i--) {
			final TableColumn tc = f_table.getColumn(order[i]);
			final ColumnData cd = (ColumnData) tc.getData();
			if (!cd.isVisible() || cd.getSort() == ColumnSort.UNSORTED) {
				continue; // Nothing to sort
			}
			if (c == null) {
				c = cd;
			} else {
				final Comparator<FindingData> oldCompare = c;
				c = new Comparator<FindingData>() {
					public int compare(final FindingData o1,
							final FindingData o2) {
						final int result = cd.compare(o1, o2);
						if (result == 0) {
							return oldCompare.compare(o1, o2);
						}
						return result;
					}

					@Override
					public String toString() {
						return cd.toString() + ", " + oldCompare.toString();
					}
				};
			}
		}
		if (c == null) {
			c = getDefaultColumn(); // The default sort
		}
		// System.out.println("Sort order = "+c);
		Collections.sort(f_rows, c);
		// Update row indices
		int i = 0;
		for(FindingData data : f_rows) {
			data.index = i;
			i++;
		}
	}

	private boolean createTableColumns = false;

	private void createTableColumns() {
		createTableColumns = true;
		final int[] order = new int[getSelection().getNumColumns()];
		int i = 0;
		for (final Column c : getSelection().getColumns()) {
			final ColumnData data = (ColumnData) c;
			final TableColumn tc = new TableColumn(f_table, data.getAlignment());
			tc.setText(data.getName());
			tc.setData(data);
			tc.setMoveable(true);
			tc.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
					// Toggle sort
					switch (data.getSort()) {
					case SORT_DOWN:
					default:
						data.setSort(ColumnSort.UNSORTED);
						break;
					case SORT_UP:
						data.setSort(ColumnSort.SORT_DOWN);
						break;
					case UNSORTED:
						data.setSort(ColumnSort.SORT_UP);
						break;
					}
					updateTableContents();
				}
			});
			tc.addControlListener(new ControlListener() {
				public void controlMoved(final ControlEvent e) {
					if (!createTableColumns && !updateTableColumns) {
						final int[] currentOrder = f_table.getColumnOrder();
						boolean changed = false;

						// Update all the columns
						final TableColumn[] columns = f_table.getColumns();
						for (int i = 0; i < currentOrder.length; i++) {
							final TableColumn tc = columns[currentOrder[i]];
							final ColumnData data = (ColumnData) tc.getData();
							if (i != data.getIndex()) {
								changed = true;
								// System.out.println(data.getIndex() + " -> " +
								// i);
								data.setIndex(i);
							}
						}
						if (changed) {
							updateTableContents();
						}
					}
				}

				public void controlResized(final ControlEvent e) {
					if (!updateTableColumns) {
						saveColumnAppearance(data, tc);
					}
				}
			});			
			order[data.getIndex()] = i;
			i++;
		}
		f_table.setColumnOrder(order);
		createTableColumns = false;
	}

	/**
	 * To be called after f_rows has been initialized Sync'd by
	 * updateTableContents()
	 */
	private boolean loadColumnAppearance(final TableColumn tc) {
		final ColumnData data = (ColumnData) tc.getData();

		tc.setAlignment(data.getAlignment());
		// System.out.println("align = "+tc.getAlignment());
		tc.setResizable(data.isVisible());
		if (data.isVisible()) {
			if (data.getWidth() < 0) {
				data.setWidth(computeValueWidth(data));
			}
			tc.setWidth(data.getWidth());
		} else {
			tc.setWidth(0);
		}
		Image img;
		switch (data.getSort()) {
		case SORT_DOWN:
			img = SLImages.getImage(CommonImages.IMG_SORT_DOWN);
			break;
		case SORT_UP:
			img = SLImages.getImage(CommonImages.IMG_SORT_UP);
			break;
		case UNSORTED:
		default:
			img = null;
			break;
		}
		tc.setImage(img);
		return data.isVisible();
	}

	static void saveColumnAppearance(final ColumnData data, final TableColumn tc) {
		// System.out.println("width = "+tc.getWidth());
		data.setWidth(tc.getWidth());
	}

	/*
	 * private static void setColumnVisible(TableColumn tc, boolean visible) {
	 * ColumnData data = (ColumnData) tc.getData(); if (visible) {
	 * tc.setWidth(data.width); tc.setResizable(true); } else { data.width =
	 * tc.getWidth(); tc.setWidth(0); tc.setResizable(false); } }
	 */
	private boolean updateTableColumns = false;

	/**
	 * Sync'd by updateTableContents()
	 */
	private void updateTableColumns() {
		int numVisible = 0;
		TableColumn lastVisible = null;
		updateTableColumns = true;

		// int i = 0;
		for (final TableColumn tc : f_table.getColumns()) {
			// System.out.println(i);
			if (loadColumnAppearance(tc)) {
				numVisible++;
				lastVisible = tc;
			}
			// i++;
		}
		if (numVisible == 1) {
			final ColumnData cd = (ColumnData) lastVisible.getData();
			lastVisible.setWidth(computeValueWidth(cd));
		}
		updateTableColumns = false;
		f_table.setHeaderVisible(numVisible > 1);
		f_table.pack();
	}

	/**
	 * This actually finds the longest item, and creates a real TableItem for
	 * that item to ensure that the table gets sized properly
	 * 
	 * Sync'd by updateTableContents()
	 */
	int computeValueWidth() {
		return computeValueWidth(getDefaultColumn());
	}

	ColumnData getDefaultColumn() {
		final String name = f_columnPrototypes.get(0).getName();
		return (ColumnData) getSelection().getColumn(name);
	}

	private static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);

	int computeValueWidth(final ColumnData cd) {
		final Image imageForGC = new Image(null, 1, 1);
		final GC gc = new GC(imageForGC);
		int longest = 0;
		FindingData longestData = null;
		int longestIndex = -1;
		gc.setFont(f_table.getFont());
		for (final FindingData data : f_rows) {
			final Point size = gc.textExtent(cd.getText(data));
			final Image img = cd.getImage(data);
			final Rectangle rect = (img == null) ? ZERO : img.getBounds();
			final int width = size.x + rect.width;
			if (width > longest) {
				longest = width;
				longestData = data;
				longestIndex = data.index;
			}
		}
		gc.dispose();
		imageForGC.dispose();
		if (longestData != null) {
			if (longestIndex >= f_table.getItemCount()) {
				LOG.warning("Got index outside of table: " + longestIndex
						+ ", " + f_table.getItemCount());
			} else {
				initTableItem(longestIndex, longestData, f_table.getItem(longestIndex));
			}
		}

		final int PAD = 30;
		if (longest < 25) {
			return PAD;
		}
		final int result = longest + PAD;
		return result;
	}

	private void initTableItem(final int i, final FindingData data, 
			                   final TableItem item) {
		if (i != data.index) {
			// Now set, because we're sorting
			
			// FIX data.index = i;			
			throw new IllegalArgumentException(i+" != data.index: "+data.index);
		}
		item.setData(data);

		// Init columns
		// int numVisible = 0;
		int j = 0;
		for (final TableColumn tc : f_table.getColumns()) {
			final ColumnData cd = (ColumnData) tc.getData();
			item.setText(j, cd.getText(data));
			item.setImage(j, cd.getImage(data));
			/*
			 * if (tc.getResizable()) { numVisible++; }
			 */
			j++;
		}
	}

	private void setupMenu(final Menu menu) {
		final MenuItem set = new MenuItem(menu, SWT.CASCADE);
		set.setText("Set Importance");
		set.setImage(SLImages
				.getImage(CommonImages.IMG_ASTERISK_DIAMOND_ORANGE));

		/*
		 * Quick audit
		 */

		final MenuItem quickAudit = new MenuItem(menu, SWT.PUSH);
		quickAudit.setText("Mark As Examined by Me");
		quickAudit.setImage(SLImages
				.getImage(CommonImages.IMG_SIERRA_STAMP_SMALL));

		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem filterFindingTypeFromScans = new MenuItem(menu, SWT.PUSH);
		filterFindingTypeFromScans
				.setText("Filter Findings Of This Type From Future Local Scans");

		new MenuItem(menu, SWT.SEPARATOR);

		final Menu importanceMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
		set.setMenu(importanceMenu);
		final MenuItem setCritical = new MenuItem(importanceMenu, SWT.PUSH);
		setCritical.setText(Importance.CRITICAL.toStringSentenceCase());
		setCritical.setImage(SLImages
				.getImage(CommonImages.IMG_ASTERISK_ORANGE_100));
		final MenuItem setHigh = new MenuItem(importanceMenu, SWT.PUSH);
		setHigh.setText(Importance.HIGH.toStringSentenceCase());
		setHigh
				.setImage(SLImages
						.getImage(CommonImages.IMG_ASTERISK_ORANGE_75));
		final MenuItem setMedium = new MenuItem(importanceMenu, SWT.PUSH);
		setMedium.setText(Importance.MEDIUM.toStringSentenceCase());
		setMedium.setImage(SLImages
				.getImage(CommonImages.IMG_ASTERISK_ORANGE_50));
		final MenuItem setLow = new MenuItem(importanceMenu, SWT.PUSH);
		setLow.setText(Importance.LOW.toStringSentenceCase());
		setLow.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_25));
		final MenuItem setIrrelevant = new MenuItem(importanceMenu, SWT.PUSH);
		setIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
		setIrrelevant.setImage(SLImages
				.getImage(CommonImages.IMG_ASTERISK_ORANGE_0));

		final MenuItem export = new MenuItem(menu, SWT.PUSH);
		export.setText("Export...");
		export.setImage(SLImages.getImage(CommonImages.IMG_EXPORT));

		final AtomicBoolean menuLocked = new AtomicBoolean();
		menu.addListener(SWT.Hide, new Listener() {
			public void handleEvent(final Event event) {
				if (menuLocked.get()) {
					rowsLock.readLock().unlock();
					menuLocked.set(false);
				}
			}
		});
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(final Event event) {
				final int[] itemIndices = f_table.getSelectionIndices();
				final boolean findingSelected = itemIndices.length > 0;
				final boolean locked = findingSelected ? rowsLock.readLock()
						.tryLock() : false;
				final boolean activated = findingSelected && locked;
				if (locked) {
					menuLocked.set(true);
				}
				set.setEnabled(activated);
				quickAudit.setEnabled(activated);
				filterFindingTypeFromScans.setEnabled(activated);
				export.setEnabled(activated);

				if (activated) {
					String importanceSoFar = null;
					String findingTypeSoFar = null;
					for (final int index : itemIndices) {
						final FindingData data = f_rows.get(index);
						final String importance = data.f_importance
								.toStringSentenceCase();
						if (importanceSoFar == null) {
							importanceSoFar = importance;
						} else if (!importanceSoFar.equals(importance)) {
							importanceSoFar = ""; // More than one
						}
						// Otherwise, it's all the same so far

						final String findingType = data.f_findingType;
						if (findingTypeSoFar == null) {
							findingTypeSoFar = findingType;
						} else if (!findingTypeSoFar.equals(findingType)) {
							findingTypeSoFar = ""; // More than one
						}
					}
					final String currentImportance = importanceSoFar;
					final String currentFindingType = findingTypeSoFar;
					setCritical.setData(itemIndices);
					setHigh.setData(itemIndices);
					setMedium.setData(itemIndices);
					setLow.setData(itemIndices);
					setIrrelevant.setData(itemIndices);
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
					quickAudit.setData(itemIndices);
					if ("".equals(currentFindingType)) {
						filterFindingTypeFromScans.setEnabled(false);
					} else {
						filterFindingTypeFromScans.setEnabled(true);
						filterFindingTypeFromScans.setData(itemIndices);
					}
				}
			}
		});

		final Listener changeImportance = new SelectionListener() {
			private Importance getImportance(final MenuItem item) {
				return Importance.valueOf(item.getText().toUpperCase());
			}

			@Override
			protected void handleFinding(final MenuItem item,
					final FindingData data) {
				FindingMutationUtility.asyncChangeImportance(data.f_findingId,
						data.f_importance, getImportance(item));
			}

			@Override
			protected void handleFindings(final MenuItem item,
					final FindingData data, final List<Long> ids) {
				FindingMutationUtility.asyncChangeImportance(ids,
						getImportance(item));
			}
		};
		setCritical.addListener(SWT.Selection, changeImportance);
		setHigh.addListener(SWT.Selection, changeImportance);
		setMedium.addListener(SWT.Selection, changeImportance);
		setLow.addListener(SWT.Selection, changeImportance);
		setIrrelevant.addListener(SWT.Selection, changeImportance);

		quickAudit.addListener(SWT.Selection, new SelectionListener() {
			@Override
			protected void handleFinding(final MenuItem item,
					final FindingData data) {
				FindingMutationUtility.asyncComment(data.f_findingId,
						FindingDetailsMediator.STAMP_COMMENT);
			}

			@Override
			protected void handleFindings(final MenuItem item,
					final FindingData data, final List<Long> ids) {
				FindingMutationUtility.asyncComment(ids,
						FindingDetailsMediator.STAMP_COMMENT);
			}
		});

		filterFindingTypeFromScans.addListener(SWT.Selection,
				new SelectionListener() {
					@Override
					protected void handleFinding(final MenuItem item,
							final FindingData data) {
						FindingMutationUtility.asyncFilterFindingTypeFromScans(
								data.f_findingId, data.f_findingType);
					}

					@Override
					protected void handleFindings(final MenuItem item,
							final FindingData data, final List<Long> ids) {
						// Note that these findings should all have the same
						// type
						// (based on code in context menu's listener)
						if (ids.size() > 1) {
							FindingMutationUtility
									.asyncFilterFindingTypeFromScans(
											ids.get(0), data.f_findingType);
						}
						/*
						 * FindingMutationUtility.asyncFilterFindingTypeFromScans
						 * ( ids, data.f_findingTypeId);
						 */
					}
				});

		export.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				final ExportFindingSetDialog dialog = new ExportFindingSetDialog(
						SWTUtility.getShell(), getQuery());
				dialog.open();
			}
		});
	}

	/**
	 * Assumes that the menu show/hide code will acquire/release the rows lock
	 */
	private abstract class SelectionListener implements Listener {
		public final void handleEvent(final Event event) {
			if (event.widget instanceof MenuItem) {
				final MenuItem item = (MenuItem) event.widget;
				if (item.getData() instanceof int[]) {
					final int[] itemIndices = (int[]) item.getData();
					final FindingData data = f_rows.get(itemIndices[0]);
					if (itemIndices.length == 1) {
						handleFinding(item, data);
					} else {
						final List<Long> ids = extractFindingIds(itemIndices);
						handleFindings(item, data, ids);
					}
				}
			}
		}

		protected abstract void handleFinding(MenuItem item, FindingData data);

		protected abstract void handleFindings(MenuItem item, FindingData data,
				List<Long> ids);
	}

	private void refreshDisplay() {
		final UIJob job = new SLUIJob() {
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor) {
				if (f_table == null) {
					final int addAfterColumn = getPreviousColumn()
							.getColumnIndex();
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

	/**
	 * Sync'd by the SelectionListener
	 * 
	 * @param itemIndices
	 * @return
	 */
	private List<Long> extractFindingIds(final int[] itemIndices) {
		final List<Long> ids = new ArrayList<Long>(itemIndices.length);
		final int startSize = f_rows.size();
		for (final int ti : itemIndices) {
			final int size = f_rows.size();
			if (startSize != size) {
				LOG.severe("Number of rows changed from " + startSize + " to "
						+ size);
				continue;
			} else if (ti >= size) {
				LOG.severe("Index out of bounds: " + ti + " >= " + size);
				continue;
			}
			final FindingData fd = f_rows.get(ti);
			if (fd != null) {
				ids.add(fd.f_findingId);
			}
		}
		return ids;
	}

	@Override
	void selectAll() {
		if (f_table.isFocusControl()) {
			f_table.selectAll();
		} else {
			super.selectAll();
		}
	}

	private volatile IFindingsObserver observer;

	public void addObserver(final IFindingsObserver o) {
		observer = o;
	}

	private void notifyObserversOfLimitedFindings(final boolean isLimited) {
		if (observer != null) {
			observer.findingsLimited(isLimited);
		}
	}

	private void notifyObserversOfDispose() {
		if (observer != null) {
			observer.findingsDisposed();
		}
	}

	private static final Set<MListOfFindingsColumn> f_tables = new HashSet<MListOfFindingsColumn>();

	public void columnsChanged(final Selection selection, final Column c) {
		// Right now, handle the fact that the visible columns changed
		final Table t = f_table;
		if (t != null) {
			if (t.isDisposed()) {
				return;
			}
			if (!c.isVisible()) {
				// No longer visible, so save column width
				for (final TableColumn tc : t.getColumns()) {
					if (c == tc.getData()) {
						saveColumnAppearance((ColumnData) c, tc);
					}
				}
			}
			updateTableContents();
		}
	}

	public static Map<String, Column> createColumns() {
		final Map<String, Column> result = new HashMap<String, Column>();
		for (final ColumnData data : f_columnPrototypes) {
			final ColumnData c = data.clone();
			// FIX to remember column ordering
			// FIX is this needed w/ clone?
			// c.configure(data.visible, data.width, data.sort, data.index);
			result.put(data.getName(), c);
		}
		return result;
	}
}
