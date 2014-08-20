package com.surelogic.sierra.client.eclipse.views.selection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.SystemUtils;
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

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadConfined;
import com.surelogic.common.CommonImages;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.CascadingList;
import com.surelogic.common.ui.CascadingList.IColumn;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.JDTUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.selection.Column;
import com.surelogic.sierra.client.eclipse.model.selection.ColumnSort;
import com.surelogic.sierra.client.eclipse.model.selection.FindingData;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsMediator;
import com.surelogic.sierra.client.eclipse.views.FindingDetailsView;
import com.surelogic.sierra.tool.message.Importance;

public final class MListOfFindingsColumn extends MColumn implements ISelectionObserver {

  @ThreadConfined
  private Table f_table = null;

  @ThreadConfined
  @NonNull
  private RowData f_data = new RowData();

  MListOfFindingsColumn(final CascadingList cascadingList, final Selection selection, final MColumn previousColumn) {
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
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        MListOfFindingsColumn.super.initOfNextColumnComplete();
        notifyObserversOfLimitedFindings(f_data.f_rowsAreCutoff);
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

  @Override
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
          final Job job = new AbstractSierraDatabaseJob("Refreshing list of findings") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
              RowData data = null;
              try {
                data = refreshData();
                if (data != null) {
                  refreshDisplay(data);
                }
              } catch (final Exception e) {
                final int errNo = 60;
                final String msg = I18N.err(errNo);
                return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
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

  /**
   * A helper class to hold the resulting rows from the database query as well
   * as a flag to indicate if all the rows are included or the query was cutoff
   * by the row limit set by the user.
   */
  private static class RowData {
    /**
     * This value is {@code true} if the rows displayed is limited by the cutoff
     * limit for rows set in the user preferences.
     */
    final boolean f_rowsAreCutoff;
    final List<FindingData> f_rows;

    RowData() {
      f_rows = Collections.emptyList();
      f_rowsAreCutoff = false;
    }

    RowData(boolean rowsAreCutoff, List<FindingData> rows) {
      f_rowsAreCutoff = rowsAreCutoff;
      f_rows = rows;
    }
  }

  /**
   * @return true if updating
   */
  public RowData refreshData() {
    final String query = getQuery();
    // System.out.println(query);
    try {
      final Connection c = Data.getInstance().readOnlyConnection();
      try {
        final Statement st = c.createStatement();
        try {
          final ResultSet rs = st.executeQuery(query);
          final ArrayList<FindingData> rows = new ArrayList<FindingData>();
          boolean rowsAreCutoff = false;
          final int findingsListLimit = EclipseUtility.getIntPreference(SierraPreferencesUtility.FINDINGS_LIST_LIMIT);
          int rowCount = 0;
          while (rs.next()) {
            if (rowCount < findingsListLimit) {
              final FindingData data = new FindingData(rs.getLong(3), rs.getString(1), Importance.valueOf(rs.getString(2)
                  .toUpperCase()), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7), rs.getString(8),
                  rs.getString(9), rs.getString(10));
              rows.add(data);
              rowCount++;
            } else {
              rowsAreCutoff = true;
              break;
            }
          }
          return new RowData(rowsAreCutoff, rows);
        } finally {
          st.close();
        }
      } finally {
        c.close();
      }
    } catch (final SQLException e) {
      SLLogger.getLogger().log(Level.SEVERE, "Query failed to read selected findings", e);
    }
    return null;
  }

  /**
   * Generates a query that is used in this class and by the export of findings.
   * The category and tool are only used by the export.
   * <p>
   * The design here is suspect and probably needs some re-work.
   *
   * @return Query on the overview of findings.
   */
  public String getQuery() {
    final StringBuilder b = new StringBuilder();
    final Selection s = getSelection();
    final String query = s.usesJoin() ? "FindingsSelectionView.showJoin" : "FindingsSelectionView.show";
    b.append(QB.get(query, getSelection().getWhereClause()));
    return b.toString();
  }

  private final KeyListener f_keyListener = new KeyListener() {
    @Override
    public void keyPressed(final KeyEvent e) {
      if (e.character == 0x01 && f_table != null) {
        f_table.selectAll();
        e.doit = false; // Handled
      } else if (e.keyCode == SWT.ARROW_LEFT) {
        getPreviousColumn().forceFocus();
        e.doit = false; // Handled
      } else if (e.keyCode == SWT.ARROW_RIGHT) {
        e.doit = false; // Handled
      }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
      // Nothing to do
    }
  };

  private final Listener f_singleClick = new Listener() {
    @Override
    public void handleEvent(final Event event) {
      final TableItem item = (TableItem) event.item;
      if (item != null) {
        final FindingData data = (FindingData) item.getData();
        if (data != null) {
          saveAndSelectFindingInOtherViews(data);
        } else {
          LOG.severe("No data for " + item.getText(0));
        }
      }
    }
  };

  /*
   * Only call from the UI thread
   */
  private void saveAndSelectFindingInOtherViews(@NonNull final FindingData data) {
    /*
     * Ensure the view is visible but don't change the focus.
     */
    FindingDetailsView.findingSelected(data.f_findingId, false);
    /*
     * Attempt to show the result in the editor
     */
    JDTUIUtility.tryToOpenInEditor(data.f_projectName, data.f_packageName, data.f_typeName, data.f_lineNumber);
  }

  private final IColumn f_iColumn = new IColumn() {
    @Override
    public Composite createContents(final Composite panel) {
      f_table = new Table(panel, SWT.FULL_SELECTION | SWT.MULTI);
      f_table.setLinesVisible(true);
      f_table.addListener(SWT.Selection, f_singleClick);
      f_table.addKeyListener(f_keyListener);
      f_table.setItemCount(0);
      createTableColumns();

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

    f_table.setRedraw(false);

    // save the selected finding ids
    final Set<FindingData> pastSelected = getSelectedItems(f_table);
    final Set<Long> pastSelectedIds = new HashSet<Long>(pastSelected.size());
    for (final FindingData data : pastSelected) {
      pastSelectedIds.add(data.f_findingId);
    }

    f_table.removeAll();

    sortModelBasedOnColumns();

    for (final FindingData data : f_data.f_rows) {
      final TableItem item = new TableItem(f_table, SWT.NONE);
      initializeTableItem(data, item);
    }

    for (TableColumn column : f_table.getColumns()) {
      column.pack();
    }

    updateTableColumns();

    f_table.layout();

    final boolean showSelection = selectFindingsInTableOrUseNear(pastSelectedIds, 0);

    f_table.setRedraw(true);
    /*
     * Fix to bug 1115 (an XP specific problem) where the table was redrawn with
     * lines through the row text. Aaron Silinskas found that a second call
     * seemed to fix the problem (with a bit of flicker).
     */
    if (SystemUtils.IS_OS_WINDOWS_XP) {
      f_table.setRedraw(true);
    }
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        /*
         * We need the call below because OS X doesn't send the resize to the
         * cascading list.
         */
        getCascadingList().fixupSize();
        if (showSelection) {
          f_table.showSelection();

          // avoid scroll bar position being to the right
          f_table.showColumn(f_table.getColumn(0));
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();

  }

  private void sortModelBasedOnColumns() {
    System.out.println("sortModelBasedOnColumns():");
    Comparator<FindingData> c = null;
    // Traverse order backwards to construct proper comparator
    final int[] order = f_table.getColumnOrder();
    for (int i = order.length - 1; i >= 0; i--) {
      final TableColumn tc = f_table.getColumn(order[i]);
      final Column column = (Column) tc.getData();
      if (!column.isVisible() || column.getSort() == ColumnSort.UNSORTED) {
        continue; // Nothing to sort
      }
      if (c == null) {
        c = column.getFindingComparator();
      } else {
        final Comparator<FindingData> oldCompare = c;
        c = new Comparator<FindingData>() {
          @Override
          public int compare(final FindingData o1, final FindingData o2) {
            final int result = column.getFindingComparator().compare(o1, o2);
            if (result == 0) {
              return oldCompare.compare(o1, o2);
            } else
              return result;
          }

          @Override
          public String toString() {
            return column.toString() + ", " + oldCompare.toString();
          }
        };
      }
    }
    if (c == null) {
      c = getDefaultColumn().getFindingComparator(); // The default sort
    }
    System.out.println(" --Sort order = " + c);
    Collections.sort(f_data.f_rows, c);
  }

  private void initializeTableItem(final FindingData data, final TableItem item) {
    item.setData(data);

    // Setup data in all the columns
    int j = 0;
    for (final TableColumn tc : f_table.getColumns()) {
      final Column column = (Column) tc.getData();
      item.setText(j, column.getCellProvider().getLabel(data));
      item.setImage(j, column.getCellProvider().getImage(data));
      j++;
    }
  }

  private boolean updateTableColumns = false;

  private void updateTableColumns() {
    int numVisible = 0;
    TableColumn lastVisible = null;
    updateTableColumns = true;

    for (final TableColumn tc : f_table.getColumns()) {
      if (loadColumnAppearance(tc)) {
        numVisible++;
        lastVisible = tc;
      }
    }
    if (numVisible == 1) {
      final Column column = (Column) lastVisible.getData();
      lastVisible.setWidth(computeValueWidth(column));
    }
    updateTableColumns = false;
    f_table.setHeaderVisible(numVisible > 1);
    f_table.pack();
  }

  /**
   * Attempts to select the passed set of finding ids, or if none are available
   * a finding id that should be near the old selection.
   * 
   * @param findingIdSet
   *          the old set of selected finding ids to try and select as much as
   *          we can again in the table.
   * @param nearFindingId
   *          a finding id near the selection to try to select.
   * @return {@code true} if anything could be selected, {@code false} if
   *         nothing was selected.
   */
  private boolean selectFindingsInTableOrUseNear(Set<Long> findingIdSet, long nearFindingId) {
    final List<TableItem> toSelectInTable = new ArrayList<TableItem>();
    @Nullable
    TableItem nearTableItem = null;

    /*
     * look through the table
     */
    for (TableItem i : f_table.getItems()) {
      final long findingId = ((FindingData) i.getData()).f_findingId;
      if (findingIdSet.contains(findingId)) {
        toSelectInTable.add(i);
      }
      if (findingId == nearFindingId) {
        nearTableItem = i;
      }
    }
    /*
     * if we didn't find any of the old selection try to use the finding id near
     * the old selection, if possible
     */
    if (toSelectInTable.isEmpty() && nearTableItem != null) {
      toSelectInTable.add(nearTableItem);
    }

    /*
     * actually make the selection
     */
    if (!toSelectInTable.isEmpty()) {
      f_table.setSelection(toSelectInTable.toArray(new TableItem[toSelectInTable.size()]));
      return true;
    } else
      return false;
  }

  private static Set<FindingData> getSelectedItems(Table table) {
    if (table.getSelectionCount() == 0) {
      return Collections.emptySet();
    }
    final Set<FindingData> selected = new HashSet<FindingData>();
    for (TableItem item : table.getSelection()) {
      selected.add((FindingData) item.getData());
    }
    return selected;
  }

  private boolean createTableColumns = false;

  private void createTableColumns() {
    createTableColumns = true;
    final int[] order = new int[getSelection().getColumns().size()];
    int i = 0;
    for (final Column c : getSelection().getColumns()) {
      final TableColumn tc = new TableColumn(f_table, c.getSwtAlignment());
      tc.setText(c.getTitle());
      tc.setData(c);
      tc.setMoveable(true);
      tc.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(final Event event) {
          // Toggle sort
          switch (c.getSort()) {
          case SORT_DOWN:
          default:
            c.setSort(ColumnSort.UNSORTED);
            break;
          case SORT_UP:
            c.setSort(ColumnSort.SORT_DOWN);
            break;
          case UNSORTED:
            c.setSort(ColumnSort.SORT_UP);
            break;
          }
          updateTableContents();
        }
      });
      tc.addControlListener(new ControlListener() {
        @Override
        public void controlMoved(final ControlEvent e) {
          if (!createTableColumns && !updateTableColumns) {
            final int[] currentOrder = f_table.getColumnOrder();
            boolean changed = false;

            // Update all the columns
            final TableColumn[] columns = f_table.getColumns();
            for (int i = 0; i < currentOrder.length; i++) {
              final TableColumn tc = columns[currentOrder[i]];
              final Column column = (Column) tc.getData();
              if (i != column.getIndex()) {
                changed = true;
                System.out.println(column.getTitle() + ":" + column.getIndex() + " -> " + i);
                column.setIndex(i);
              }
            }
            if (changed) {
              updateTableContents();
            }
          }
        }

        @Override
        public void controlResized(final ControlEvent e) {
          if (!updateTableColumns) {
            saveColumnAppearance(c, tc);
          }
        }
      });
      order[c.getIndex()] = i;
      i++;
    }
    f_table.setColumnOrder(order);
    createTableColumns = false;
  }

  /**
   * To be called after f_rows has been initialized. Sync'd by
   * updateTableContents()
   */
  private boolean loadColumnAppearance(final TableColumn tc) {
    final Column column = (Column) tc.getData();

    tc.setAlignment(column.getSwtAlignment());
    tc.setResizable(column.isVisible());
    if (column.isVisible()) {
      if (column.getWidth() < 30) {
        column.setWidth(computeValueWidth(column));
      }
      tc.setWidth(column.getWidth());
    } else {
      // make invisible by using a width of zero
      tc.setWidth(0);
    }
    final Image img;
    switch (column.getSort()) {
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
    setTableColumnVisible(tc, column.isVisible());
    return column.isVisible();
  }

  static void saveColumnAppearance(final Column column, final TableColumn tc) {
    column.setWidth(tc.getWidth());
  }

  private static void setTableColumnVisible(TableColumn tc, boolean visible) {
    final Column column = (Column) tc.getData();
    if (visible) {
      tc.setWidth(column.getWidth());
      tc.setResizable(true);
    } else {
      column.setWidth(tc.getWidth());
      tc.setWidth(0);
      tc.setResizable(false);
    }
  }

  Column getDefaultColumn() {
    return getSelection().getColumnByTitle(Selection.SUMMARY_COLUMN);
  }

  private static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);

  /**
   * Goes through all the rows for a particular column and determines the
   * optional width.
   * 
   * TODO IS THIS NEEDED? WHY NOT PACK
   * 
   * @param column
   *          a column.
   * @return the ideal width for the column.
   */
  private int computeValueWidth(final Column column) {
    final Image imageForGC = new Image(null, 1, 1);
    final GC gc = new GC(imageForGC);
    int longest = 0;
    gc.setFont(f_table.getFont());
    for (final FindingData data : f_data.f_rows) {
      final Point size = gc.textExtent(column.getCellProvider().getLabel((data)));
      final Image img = column.getCellProvider().getImage(data);
      final Rectangle rect = img == null ? ZERO : img.getBounds();
      final int width = size.x + rect.width;
      if (width > longest) {
        longest = width;
      }
    }
    gc.dispose();
    imageForGC.dispose();

    final int PAD = 30;
    if (longest < 25) {
      return PAD;
    }
    final int result = longest + PAD;
    return result;
  }

  private void setupMenu(final Menu menu) {
    final MenuItem set = new MenuItem(menu, SWT.CASCADE);
    set.setText("Set Importance");
    set.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_DIAMOND_ORANGE));

    /*
     * Quick audit
     */

    final MenuItem quickAudit = new MenuItem(menu, SWT.PUSH);
    quickAudit.setText("Mark As Examined by Me");
    quickAudit.setImage(SLImages.getImage(CommonImages.IMG_SIERRA_STAMP_SMALL));

    new MenuItem(menu, SWT.SEPARATOR);

    final MenuItem filterFindingTypeFromScans = new MenuItem(menu, SWT.PUSH);
    filterFindingTypeFromScans.setText("Filter Findings Of This Type From Future Local Scans");

    new MenuItem(menu, SWT.SEPARATOR);

    final Menu importanceMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
    set.setMenu(importanceMenu);
    final MenuItem setCritical = new MenuItem(importanceMenu, SWT.PUSH);
    setCritical.setText(Importance.CRITICAL.toStringSentenceCase());
    setCritical.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_100));
    final MenuItem setHigh = new MenuItem(importanceMenu, SWT.PUSH);
    setHigh.setText(Importance.HIGH.toStringSentenceCase());
    setHigh.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_75));
    final MenuItem setMedium = new MenuItem(importanceMenu, SWT.PUSH);
    setMedium.setText(Importance.MEDIUM.toStringSentenceCase());
    setMedium.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_50));
    final MenuItem setLow = new MenuItem(importanceMenu, SWT.PUSH);
    setLow.setText(Importance.LOW.toStringSentenceCase());
    setLow.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_25));
    final MenuItem setIrrelevant = new MenuItem(importanceMenu, SWT.PUSH);
    setIrrelevant.setText(Importance.IRRELEVANT.toStringSentenceCase());
    setIrrelevant.setImage(SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_0));

    final MenuItem export = new MenuItem(menu, SWT.PUSH);
    export.setText("Export...");
    export.setImage(SLImages.getImage(CommonImages.IMG_EXPORT));

    menu.addListener(SWT.Show, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final int[] itemIndices = f_table.getSelectionIndices();
        final boolean findingSelected = itemIndices.length > 0;
        final boolean activated = findingSelected;
        set.setEnabled(activated);
        quickAudit.setEnabled(activated);
        filterFindingTypeFromScans.setEnabled(activated);
        export.setEnabled(activated);

        if (activated) {
          String importanceSoFar = null;
          String findingTypeSoFar = null;
          for (final int index : itemIndices) {
            final FindingData data = f_data.f_rows.get(index);
            final String importance = data.f_importance.toStringSentenceCase();
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
          setCritical.setEnabled(!currentImportance.equals(setCritical.getText()));
          setHigh.setEnabled(!currentImportance.equals(setHigh.getText()));
          setMedium.setEnabled(!currentImportance.equals(setMedium.getText()));
          setLow.setEnabled(!currentImportance.equals(setLow.getText()));
          setIrrelevant.setEnabled(!currentImportance.equals(setIrrelevant.getText()));
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
      protected void handleFinding(final MenuItem item, final FindingData data) {
        FindingMutationUtility.asyncChangeImportance(data.f_findingId, data.f_importance, getImportance(item));
      }

      @Override
      protected void handleFindings(final MenuItem item, final FindingData data, final List<Long> ids) {
        FindingMutationUtility.asyncChangeImportance(ids, getImportance(item));
      }
    };
    setCritical.addListener(SWT.Selection, changeImportance);
    setHigh.addListener(SWT.Selection, changeImportance);
    setMedium.addListener(SWT.Selection, changeImportance);
    setLow.addListener(SWT.Selection, changeImportance);
    setIrrelevant.addListener(SWT.Selection, changeImportance);

    quickAudit.addListener(SWT.Selection, new SelectionListener() {
      @Override
      protected void handleFinding(final MenuItem item, final FindingData data) {
        FindingMutationUtility.asyncComment(data.f_findingId, FindingDetailsMediator.STAMP_COMMENT);
      }

      @Override
      protected void handleFindings(final MenuItem item, final FindingData data, final List<Long> ids) {
        FindingMutationUtility.asyncComment(ids, FindingDetailsMediator.STAMP_COMMENT);
      }
    });

    filterFindingTypeFromScans.addListener(SWT.Selection, new SelectionListener() {
      @Override
      protected void handleFinding(final MenuItem item, final FindingData data) {
        FindingMutationUtility.asyncFilterFindingTypeFromScans(data.f_findingId, data.f_findingType);
      }

      @Override
      protected void handleFindings(final MenuItem item, final FindingData data, final List<Long> ids) {
        // Note that these findings should all have the same
        // type
        // (based on code in context menu's listener)
        if (ids.size() > 1) {
          FindingMutationUtility.asyncFilterFindingTypeFromScans(ids.get(0), data.f_findingType);
        }
        /*
         * FindingMutationUtility.asyncFilterFindingTypeFromScans ( ids,
         * data.f_findingTypeId);
         */
      }
    });

    export.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(final Event event) {
        final ExportFindingSetDialog dialog = new ExportFindingSetDialog(EclipseUIUtility.getShell(), getQuery());
        dialog.open();
      }
    });
  }

  /**
   * Assumes that the menu show/hide code will acquire/release the rows lock
   */
  private abstract class SelectionListener implements Listener {
    @Override
    public final void handleEvent(final Event event) {
      if (event.widget instanceof MenuItem) {
        final MenuItem item = (MenuItem) event.widget;
        if (item.getData() instanceof int[]) {
          final int[] itemIndices = (int[]) item.getData();
          final FindingData data = f_data.f_rows.get(itemIndices[0]);
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

    protected abstract void handleFindings(MenuItem item, FindingData data, List<Long> ids);
  }

  private void refreshDisplay(final @NonNull RowData data) {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        f_data = data;
        if (f_table == null) {
          final int addAfterColumn = getPreviousColumn().getColumnIndex();
          // create the display table
          getCascadingList().addColumnAfter(f_iColumn, addAfterColumn, false);
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
    for (final int ti : itemIndices) {
      final FindingData fd = f_data.f_rows.get(ti);
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

  @Override
  public void columnVisibilityChanged(final Selection selection, final Column c) {
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
            saveColumnAppearance((Column) c, tc);
          }
        }
      }
      updateTableContents();
    }
  }
}
