package com.surelogic.sierra.client.eclipse.views;

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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.jdbc.QB;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.EclipseUIUtility;
import com.surelogic.common.ui.JDTUIUtility;
import com.surelogic.common.ui.SLImages;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.client.eclipse.SierraUIUtility;
import com.surelogic.sierra.client.eclipse.actions.NewScanAction;
import com.surelogic.sierra.client.eclipse.actions.PreferencesAction;
import com.surelogic.sierra.client.eclipse.dialogs.ExportFindingSetDialog;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.FindingMutationUtility;
import com.surelogic.sierra.client.eclipse.model.IProjectsObserver;
import com.surelogic.sierra.client.eclipse.model.Projects;
import com.surelogic.sierra.client.eclipse.model.selection.AbstractColumnCellProvider;
import com.surelogic.sierra.client.eclipse.model.selection.Column;
import com.surelogic.sierra.client.eclipse.model.selection.ColumnSort;
import com.surelogic.sierra.client.eclipse.model.selection.FindingData;
import com.surelogic.sierra.client.eclipse.model.selection.IColumnCellProvider;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionManagerObserver;
import com.surelogic.sierra.client.eclipse.model.selection.ISelectionObserver;
import com.surelogic.sierra.client.eclipse.model.selection.Selection;
import com.surelogic.sierra.client.eclipse.model.selection.SelectionManager;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.tool.message.Importance;

public class FindingsMediator extends AbstractSierraViewMediator implements IViewUpdater, IProjectsObserver, ISelectionObserver,
    ISelectionManagerObserver {

  public static final String SUMMARY_COLUMN = "Summary";
  public static final String TOOL_COLUMN = "Tool";
  public static final String PROJECT_COLUMN = "Project";
  public static final String PACKAGE_COLUMN = "Package";
  public static final String TYPE_COLUMN = "Type";
  public static final String LINE_COLUMN = "Line";
  public static final String IMPORTANCE_COLUMN = "Importance";
  public static final String FINDING_TYPE_COLUMN = "Finding Type";

  private final SelectionManager f_manager = SelectionManager.getInstance();

  private final FindingsView f_view;
  private final Composite f_panel;
  private final Table f_resultsTable;

  private final List<Column> f_listOfFindingsColumns;

  protected FindingsMediator(FindingsView view, Composite panel, Table resultsTable) {
    super(view);
    f_view = view;
    f_panel = panel;
    f_resultsTable = resultsTable;
    ArrayList<Column> working = new ArrayList<Column>();
    fillColumns(working);
    f_listOfFindingsColumns = Collections.unmodifiableList(working);
  }

  @Override
  public void init() {
    super.init();

    f_resultsTable.addListener(SWT.Selection, f_singleClick);
    createTableColumns();
    f_resultsTable.setHeaderVisible(true);

    final Menu menu = new Menu(f_resultsTable.getShell(), SWT.POP_UP);
    f_resultsTable.setMenu(menu);
    setupMenu(menu);

    SelectionManager.getInstance().addObserver(this);
    Projects.getInstance().addObserver(this);
    notify(Projects.getInstance());
  }

  @Override
  public void dispose() {
    final Selection workingSelection = f_manager.getWorkingSelection();
    if (workingSelection != null) {
      f_manager.removeObserver(this);
    }
    Projects.getInstance().removeObserver(this);
    SelectionManager.getInstance().removeObserver(this);
    super.dispose();
  }

  @Override
  public void notify(Projects p) {
    /*
     * We are checking if there is anything in the database at all. If not we
     * show a helpful message, if so we display the findings selection page.
     */
    final boolean dataShouldShow = !p.isEmpty();

    // beware the thread context this method call might be made in.
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        if (!f_view.matchesStatus(dataShouldShow)) {
          /*
           * Only gets run when the page actually has changed.
           */
          f_view.hasData(dataShouldShow);

          if (dataShouldShow) {
            dataToDisplayChanged();
          }
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  @Override
  public String getNoDataI18N() {
    return "sierra.eclipse.noDataFindings";
  }

  @Override
  public Listener getNoDataListener() {
    return new Listener() {
      @Override
      public void handleEvent(final Event event) {
        new NewScanAction().run();
      }
    };
  }

  @Override
  public String getHelpId() {
    return "com.surelogic.sierra.client.eclipse.view-finding-details"; // TODO
  }

  @Override
  public void setFocus() {
    f_resultsTable.setFocus();
  }

  public void updateContentsForUI() {
    // nothing to do
  }

  @Override
  public void selectionChanged(Selection ignore) {
    dataToDisplayChanged();
  }

  @Override
  public void savedSelectionsChanged() {
    // Nothing to do for this view

  }

  @Override
  public void workingSelectionChanged(Selection newWorkingSelection, Selection oldWorkingSelection) {
    if (oldWorkingSelection != null)
      oldWorkingSelection.removeObserver(this);
    if (newWorkingSelection != null)
      newWorkingSelection.addObserver(this);
    selectionChanged(newWorkingSelection);
  }

  @Override
  public void showingFindings(int count, int ofPossible) {
    // TODO Auto-generated method stub
  }

  // ////////////////////////////

  @NonNull
  private RowData f_data = new RowData();

  private void dataToDisplayChanged() {
    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        if (!f_resultsTable.isDisposed()) {
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
    final int f_rowsAvailableInDb;

    RowData() {
      f_rows = Collections.emptyList();
      f_rowsAreCutoff = false;
      f_rowsAvailableInDb = 0;
    }

    RowData(boolean rowsAreCutoff, List<FindingData> rows, Selection selection) {
      f_rowsAreCutoff = rowsAreCutoff;
      f_rows = rows;
      f_rowsAvailableInDb = selection.getFindingCountPorous();
    }
  }

  /**
   * @return true if updating
   */
  RowData refreshData() {
    final Selection workingSelection = f_manager.getWorkingSelection();
    if (workingSelection == null) {
      // no selection return an empty list
      return new RowData();
    }
    final String query = getQuery(workingSelection);
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
          return new RowData(rowsAreCutoff, rows, workingSelection);
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
   * 
   * @param selection
   *          the selection
   * @return Query on the overview of findings
   * @throws IllegalArgumentException
   *           if the selection passed is null
   */
  private String getQuery(@NonNull final Selection selection) {
    if (selection == null)
      throw new IllegalArgumentException(I18N.err(44, "selection"));
    final StringBuilder b = new StringBuilder();
    final String query = selection.usesJoin() ? "FindingsSelectionView.showJoin" : "FindingsSelectionView.show";
    b.append(QB.get(query, selection.getWhereClause()));
    return b.toString();
  }

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

  private boolean createTableColumns = false;

  private void createTableColumns() {

    createTableColumns = true;
    final int[] order = new int[f_listOfFindingsColumns.size()];
    int i = 0;
    for (final Column c : f_listOfFindingsColumns) {
      final TableColumn tc = new TableColumn(f_resultsTable, c.getSwtAlignment());
      tc.setText(c.getTitle());
      tc.setData(c);
      tc.setMoveable(true);

      order[c.getIndex()] = i;
      i++;

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
            final int[] currentOrder = f_resultsTable.getColumnOrder();
            boolean changed = false;

            // Update all the columns
            final TableColumn[] columns = f_resultsTable.getColumns();
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
            /*
             * This indicates a user width preference we should save
             */
            c.setUserSetWidth(tc.getWidth());
          }
        }
      });
    }
    f_resultsTable.setColumnOrder(order);
    createTableColumns = false;
  }

  private void updateTableContents() {
    if (f_resultsTable.isDisposed()) {
      return;
    }

    f_resultsTable.setRedraw(false);

    // save the selected finding ids
    final Set<FindingData> pastSelected = getSelectedItems(f_resultsTable);
    final Set<Long> pastSelectedIds = new HashSet<Long>(pastSelected.size());
    for (final FindingData data : pastSelected) {
      pastSelectedIds.add(data.f_findingId);
    }

    f_resultsTable.removeAll();

    final boolean hasFindings = f_data.f_rows.size() > 0;

    if (hasFindings)
      sortModelBasedOnColumns();

    for (final FindingData data : f_data.f_rows) {
      final TableItem item = new TableItem(f_resultsTable, SWT.NONE);
      initializeTableItem(data, item);
    }

    if (hasFindings)
      updateTableColumns();

    final boolean showSelection = selectFindingsInTableOrUseNear(pastSelectedIds, 0);

    f_resultsTable.setRedraw(true);
    /*
     * Fix to bug 1115 (an XP specific problem) where the table was redrawn with
     * lines through the row text. Aaron Silinskas found that a second call
     * seemed to fix the problem (with a bit of flicker).
     */
    if (SystemUtils.IS_OS_WINDOWS_XP) {
      f_resultsTable.setRedraw(true);
    }

    updateCutoffWarning(f_data);

    if (showSelection) {
      final UIJob job = new SLUIJob() {
        @Override
        public IStatus runInUIThread(final IProgressMonitor monitor) {
          f_resultsTable.showSelection();

          // avoid scroll bar position being to the right
          f_resultsTable.showColumn(f_resultsTable.getColumn(0));
          return Status.OK_STATUS;
        }
      };
      job.schedule();
    }
  }

  private void updateCutoffWarning(RowData rowData) {
    // clear out old display, if any
    if (f_panel.getChildren().length > 1) {
      f_panel.getChildren()[1].dispose(); // second should info panel
    }
    if (rowData.f_rowsAreCutoff) {
      final Composite informationPanel = new Composite(f_panel, SWT.NONE);
      informationPanel.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.numColumns = 2;
      layout.verticalSpacing = 0;
      informationPanel.setLayout(layout);

      final Label warningIcon = new Label(informationPanel, SWT.NONE);
      warningIcon.setText("");
      warningIcon.setImage(SLImages.getImage(CommonImages.IMG_WARNING));
      warningIcon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

      final Link statusLink = new Link(informationPanel, SWT.NONE);
      statusLink.setText("<a>Showing " + rowData.f_rows.size() + " of " + rowData.f_rowsAvailableInDb + " possible findings</a>");
      statusLink.setToolTipText("Click to change the maximum number of findings shown in this view");
      statusLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

      statusLink.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          PreferencesUtil.createPreferenceDialogOn(null, PreferencesAction.PREF_ID, PreferencesAction.FILTER, null).open();
        }
      });

      f_panel.layout();
    }
  }

  private void sortModelBasedOnColumns() {
    System.out.println("sortModelBasedOnColumns():");
    Comparator<FindingData> c = null;
    // Traverse order backwards to construct proper comparator
    final int[] order = f_resultsTable.getColumnOrder();
    for (int i = order.length - 1; i >= 0; i--) {
      final TableColumn tc = f_resultsTable.getColumn(order[i]);
      final Column column = (Column) tc.getData();
      if (column.getSort() == ColumnSort.UNSORTED) {
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
    for (final TableColumn tc : f_resultsTable.getColumns()) {
      final Column column = (Column) tc.getData();
      item.setText(j, column.getCellProvider().getLabel(data));
      item.setImage(j, column.getCellProvider().getImage(data));
      j++;
    }
  }

  /**
   * Used to avoid listeners reacting to settings during updates of the table.
   */
  private boolean updateTableColumns = false;

  private void updateTableColumns() {
    updateTableColumns = true;

    for (final TableColumn tc : f_resultsTable.getColumns()) {
      loadColumnAppearance(tc);
    }
    updateTableColumns = false;
    f_resultsTable.pack();
  }

  /**
   * To be called after f_rows has been initialized.
   */
  private void loadColumnAppearance(final TableColumn tc) {
    final Column column = (Column) tc.getData();

    tc.setAlignment(column.getSwtAlignment());
    final int width;
    if (column.hasUserSetWidth())
      width = column.getUserSetWidth();
    else
      width = computeValueWidth(column);
    tc.setWidth(width);
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
    setTableColumnVisible(tc);
  }

  private void setTableColumnVisible(TableColumn tc) {
    final Column column = (Column) tc.getData();
    final int width;
    if (column.hasUserSetWidth())
      width = column.getUserSetWidth();
    else
      width = computeValueWidth(column);
    tc.setWidth(width);
    tc.setResizable(true);
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
    for (TableItem i : f_resultsTable.getItems()) {
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
      f_resultsTable.setSelection(toSelectInTable.toArray(new TableItem[toSelectInTable.size()]));
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

  Column getDefaultColumn() {
    return getColumnByTitle(SUMMARY_COLUMN);
  }

  private static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);

  /**
   * Goes through all the rows for a particular column and determines the
   * optional width.
   * 
   * @param column
   *          a column.
   * @return the ideal width for the column.
   */
  private int computeValueWidth(final Column column) {
    final Image imageForGC = new Image(null, 1, 1);
    final GC gc = new GC(imageForGC);
    int longest = gc.textExtent(column.getTitle()).x; // consider title
    gc.setFont(f_resultsTable.getFont());
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

    final int PAD = 40;
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
        final int[] itemIndices = f_resultsTable.getSelectionIndices();
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
        final Selection workingSelection = f_manager.getWorkingSelection();
        if (workingSelection != null) {
          final ExportFindingSetDialog dialog = new ExportFindingSetDialog(EclipseUIUtility.getShell(), getQuery(workingSelection));
          dialog.open();
        }
      }
    });
  }

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
        // update the table's contents
        updateTableContents();
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

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

  /**
   * Populates the collection of mutable column information used by this view..
   * 
   * @param listOfFindingsColumns
   *          an empty mutable list to fill.
   */
  private static void fillColumns(List<Column> listOfFindingsColumns) {

    /*
     * Summary
     */
    final Column summary = new Column(SUMMARY_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_summary;
      }

      @Override
      public Image getImage(FindingData row) {
        return SierraUIUtility.getImageFor(row.f_importance);
      }
    });
    listOfFindingsColumns.add(summary);

    /*
     * Tool
     */
    listOfFindingsColumns.add(new Column(TOOL_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_toolName;
      }

      @Override
      public Image getImage(FindingData row) {
        return SierraUIUtility.getImageForTool(row.f_toolName);
      }
    }));

    /*
     * Project
     */
    listOfFindingsColumns.add(new Column(PROJECT_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_projectName;
      }

      @Override
      public Image getImage(FindingData row) {
        return SLImages.getImageForProject(row.f_projectName);
      }
    }));

    /*
     * Package
     */
    listOfFindingsColumns.add(new Column(PACKAGE_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_packageName;
      }

      @Override
      public Image getImage(FindingData row) {
        return SLImages.getImage(CommonImages.IMG_PACKAGE);
      }
    }));

    /*
     * Type
     */
    listOfFindingsColumns.add(new Column(TYPE_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_typeName;
      }

      @Override
      public Image getImage(FindingData row) {
        return SierraUIUtility.getImageForType(row.f_projectName, row.f_packageName, row.f_typeName);
      }
    }));

    /*
     * Line
     */
    listOfFindingsColumns.add(new Column(LINE_COLUMN, new IColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return Integer.toString(row.f_lineNumber);
      }

      @Override
      public Image getImage(FindingData row) {
        return null;
      }

      @Override
      public int compareInternal(Column column, FindingData row1, FindingData row2) {
        return row1.f_lineNumber - row2.f_lineNumber;
      }
    }, SWT.RIGHT));

    /*
     * Importance
     */
    listOfFindingsColumns.add(new Column(IMPORTANCE_COLUMN, new IColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_importance.toStringSentenceCase();
      }

      @Override
      public Image getImage(FindingData row) {
        return SierraUIUtility.getImageFor(row.f_importance);
      }

      @Override
      public int compareInternal(Column column, FindingData row1, FindingData row2) {
        return row1.f_importance.ordinal() - row1.f_importance.ordinal();
      }
    }));

    /*
     * Finding type
     */
    listOfFindingsColumns.add(new Column(FINDING_TYPE_COLUMN, new AbstractColumnCellProvider() {

      @Override
      public String getLabel(FindingData row) {
        return row.f_findingType;
      }

      @Override
      public Image getImage(FindingData row) {
        return SLImages.getImage(CommonImages.IMG_INDEX_CARD);
      }
    }));

    int i = 0;
    for (Column column : listOfFindingsColumns) {
      column.setIndex(i);
      i++;
    }
  }

  /**
   * Gets the column in the list of findings display (the 'Show' results) with
   * the given title.
   * 
   * @param title
   *          a column title (i.e., the name of the column).
   * @return a column or {@code null} if none can be found for the passed title.
   */
  @Nullable
  private Column getColumnByTitle(String title) {
    if (title != null)
      for (Column column : f_listOfFindingsColumns) {
        if (title.equals(column.getTitle()))
          return column;
      }
    return null;
  }
}
