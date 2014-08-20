package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.surelogic.Nullable;
import com.surelogic.common.CommonImages;
import com.surelogic.common.core.logging.SLEclipseStatusUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.SierraUIUtility;
import com.surelogic.sierra.client.eclipse.jobs.AbstractSierraDatabaseJob;
import com.surelogic.sierra.client.eclipse.model.AbstractDatabaseObserver;
import com.surelogic.sierra.client.eclipse.model.DatabaseHub;

/**
 * Defines a selection of findings using a series of filters.
 * <p>
 * This class is thread-safe.
 */
public final class Selection extends AbstractDatabaseObserver {

  /**
   * Immutable set of all possible filters.
   */
  private static final Set<ISelectionFilterFactory> f_allFilters;
  static {
    Set<ISelectionFilterFactory> allFilters = new HashSet<ISelectionFilterFactory>();
    /*
     * Add in all the filter factories.
     */
    allFilters.add(FilterArtifactCount.FACTORY);
    allFilters.add(FilterAuditCount.FACTORY);
    allFilters.add(FilterAudited.FACTORY);
    allFilters.add(FilterAdHocFindingCategory.FACTORY);
    allFilters.add(FilterFindingType.FACTORY);
    allFilters.add(FilterImportance.FACTORY);
    allFilters.add(FilterJavaType.FACTORY);
    allFilters.add(FilterJavaPackage.FACTORY);
    allFilters.add(FilterProject.FACTORY);
    allFilters.add(FilterStatus.FACTORY);
    allFilters.add(FilterTool.FACTORY);

    f_allFilters = Collections.unmodifiableSet(allFilters);
  }

  /**
   * Gets the immutable set of all possible filters.
   * 
   * @return the immutable set of all possible filters.
   */
  public static Set<ISelectionFilterFactory> getAllFilters() {
    return f_allFilters;
  }

  public static final String SUMMARY_COLUMN = "Summary";
  public static final String TOOL_COLUMN = "Tool";
  public static final String PROJECT_COLUMN = "Project";
  public static final String PACKAGE_COLUMN = "Package";
  public static final String TYPE_COLUMN = "Type";
  public static final String LINE_COLUMN = "Line";
  public static final String IMPORTANCE_COLUMN = "Importance";
  public static final String FINDING_TYPE_COLUMN = "Finding Type";

  public static final String[] ALL_COLUMN_TITLES = { SUMMARY_COLUMN, TOOL_COLUMN, PROJECT_COLUMN, PACKAGE_COLUMN, TYPE_COLUMN,
      LINE_COLUMN, IMPORTANCE_COLUMN, FINDING_TYPE_COLUMN };
  public static final String[] TOGGLEABLE_COLUMN_TITLES = { TOOL_COLUMN, PROJECT_COLUMN, PACKAGE_COLUMN, TYPE_COLUMN, LINE_COLUMN,
      IMPORTANCE_COLUMN, FINDING_TYPE_COLUMN };

  /**
   * Used by the constructor of {@link Selection} to create the columns that are
   * able to be displayed in the list of findings portion of the quick search
   * control.
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
    summary.setVisible(true);
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

  /*
   * Constructors
   */

  Selection(SelectionManager manager) {
    assert manager != null;
    f_manager = manager;
    fillColumns(f_listOfFindingsColumns);
  }

  public Selection(Selection source) {
    synchronized (source) {
      f_manager = source.f_manager;
      f_showingFindings = source.f_showingFindings;

      Filter prev = null;
      for (Filter f : source.f_filters) {
        Filter clone = f.copyNoQuery(this, prev);
        prev = clone;
        f_filters.add(clone);
      }
      for (Column toCopy : source.f_listOfFindingsColumns) {
        f_listOfFindingsColumns.add(new Column(toCopy));
      }
    }
  }

  /**
   * This just connects this filter to the database. Making it reflect changes
   * to the database. Ensure that {@link #dispose()} is called to disconnect
   * this selection from the database when the selection is no longer used.
   */
  public void initAndSyncToDatabase() {
    DatabaseHub.getInstance().addObserver(this);
  }

  public void dispose() {
    DatabaseHub.getInstance().removeObserver(this);
    synchronized (this) {
      for (Filter f : f_filters)
        f.dispose();
      f_observers.clear();
    }
  }

  private final SelectionManager f_manager;

  public SelectionManager getManager() {
    /*
     * Not mutable so we don't need to hold a lock on this.
     */
    return f_manager;
  }

  /**
   * The ordered list of filters within this selection.
   */
  private final LinkedList<Filter> f_filters = new LinkedList<Filter>();

  /**
   * Gets the ordered list of filters managed by this Selection;
   * 
   * @return
   */
  public final List<Filter> getFilters() {
    synchronized (this) {
      return new LinkedList<Filter>(f_filters);
    }
  }

  /**
   * Indicates if the passed filter is the first filter of this selection.
   * 
   * @param filter
   *          a filter within this selection.
   * @return <code>true</code> if the passed filter is the first filter of this
   *         selection, <code>false</code> otherwise.
   */
  public boolean isFirstFilter(Filter filter) {
    return f_filters.getFirst() == filter;
  }

  /**
   * Indicates if the passed filter is the last filter of this selection.
   * 
   * @param filter
   *          a filter within this selection.
   * @return <code>true</code> if the passed filter is the last filter of this
   *         selection, <code>false</code> otherwise.
   */
  public boolean isLastFilter(Filter filter) {
    return f_filters.getLast() == filter;
  }

  /**
   * Removes all the passed filter and all subsequent filters from this
   * selection.
   * 
   * @param filter
   *          a filter within this selection, may be <code>null</code> in which
   *          case the selection is not modified.
   */
  public void emptyAfter(Filter filter) {
    if (filter == null)
      return;
    final List<Filter> disposeList = new ArrayList<Filter>();
    boolean found = false;
    synchronized (this) {
      for (Iterator<Filter> iterator = f_filters.iterator(); iterator.hasNext();) {
        final Filter next = iterator.next();
        if (filter == next)
          found = true;
        if (found) {
          disposeList.add(filter);
          iterator.remove();
        }
      }
    }
    if (found) {
      for (Filter f : disposeList) {
        f.dispose();
      }
      notifySelectionChanged();
    }
  }

  /**
   * Removes all existing filters from this selection with an index after the
   * specified index.
   * 
   * @param filterIndex
   *          the index of a filter used by this selection. A value of -1 will
   *          clear out all filters.
   */
  public void emptyAfter(int filterIndex) {
    final List<Filter> disposeList = new ArrayList<Filter>();
    boolean changed = false;
    int index = 0;
    synchronized (this) {
      for (Iterator<Filter> iterator = f_filters.iterator(); iterator.hasNext();) {
        Filter filter = iterator.next();
        if (index > filterIndex) {
          disposeList.add(filter);
          iterator.remove();
          changed = true;
        }
        index++;
      }
    }
    if (changed) {
      for (Filter f : disposeList) {
        f.dispose();
      }
      notifySelectionChanged();
    }
  }

  /**
   * Gets the number of filters used by this selection.
   * 
   * @return the number of filters used by this selection.
   */
  public int getFilterCount() {
    synchronized (this) {
      return f_filters.size();
    }
  }

  /**
   * Indicates if this selection should show the list of findings selected.
   */
  private boolean f_showingFindings = false;

  /**
   * Indicates if this selection shows the list of findings in the UI.
   * 
   * @return <code>true</code> if this selection should show the list of
   *         findings, <code>false<code> if it should not.
   */
  public boolean isShowingFindings() {
    synchronized (this) {
      return f_showingFindings;
    }
  }

  /**
   * Sets the status of this selection with regard to showing the list of
   * findings.
   * 
   * @param value
   *          <code>true</code> if this selection should show the list of
   *          findings, <code>false<code> if it should not.
   */
  public void setShowingFindings(boolean value) {
    synchronized (this) {
      f_showingFindings = value;
    }
  }

  /**
   * Constructs a filter at the end of this selections chain of filters. Adds an
   * optional observer to that filter. This method does <i>not</i> initiate the
   * query to populate the filter.
   * 
   * @param factory
   *          a filter factory used to select the filter to be constructed.
   * @param observer
   *          an observer for the new filter, may be <code>null</code> if no
   *          observer is desired.
   * @return the new filter.
   */
  public Filter construct(ISelectionFilterFactory factory, IFilterObserver observer) {
    if (factory == null)
      throw new IllegalArgumentException("factory must be non-null");
    final Filter filter;
    synchronized (this) {
      if (!getAvailableFilters().contains(factory))
        throw new IllegalArgumentException(factory.getFilterLabel() + " already used in selection");
      final Filter previous = f_filters.isEmpty() ? null : f_filters.getLast();
      filter = factory.construct(this, previous);
      f_filters.add(filter);
    }
    filter.addObserver(observer);
    return filter;
  }

  /**
   * Gets the list of filters that are not yet being used as part of this
   * selection. Any member of this result could be used to in a call to
   * {@link #construct(ISelectionFilterFactory)}.
   * 
   * @return factories for unused filters.
   */
  public List<ISelectionFilterFactory> getAvailableFilters() {
    List<ISelectionFilterFactory> result = new ArrayList<ISelectionFilterFactory>(f_allFilters);
    synchronized (this) {
      for (Filter filter : f_filters) {
        result.remove(filter.getFactory());
      }
    }
    Collections.sort(result);
    return result;
  }

  /**
   * The count of findings that this selection, based upon what its filters have
   * set to be porous, will allow through.
   * 
   * @return count of findings that this selection, based upon what its filters
   *         have set to be porous, will allow through.
   */
  public int getFindingCountPorous() {
    synchronized (this) {
      if (!f_filters.isEmpty()) {
        return f_filters.getLast().getFindingCountPorous();
      } else {
        return 0;
      }
    }
  }

  /**
   * Adds the correct <code>from</code> and <code>where</code> clause to make a
   * query get the set of findings defined by this selection from the
   * <code>FINDINGS_OVERVIEW</code> table.
   * 
   * @param b
   *          the string to mutate.
   */
  public String getWhereClause() {
    final StringBuilder b = new StringBuilder();
    synchronized (this) {
      if (!f_filters.isEmpty()) {
        final Filter last = f_filters.getLast();
        synchronized (last) {
          b.append(last.getWhereClause(true));
        }
      }
    }
    return b.toString();
  }

  public boolean usesJoin() {
    synchronized (this) {
      if (!f_filters.isEmpty()) {
        final Filter last = f_filters.getLast();
        synchronized (last) {
          return last.usesJoin();
        }
      }
    }
    return false;
  }

  /**
   * Indicates if this selection allows any possible findings through it.
   * 
   * @return <code>true</code> if the selection allows findings through it,
   *         <code>false</code> otherwise.
   */
  public boolean isPorous() {
    return getFindingCountPorous() > 0;
  }

  private final Set<ISelectionObserver> f_observers = new CopyOnWriteArraySet<ISelectionObserver>();

  public void addObserver(ISelectionObserver o) {
    if (o == null)
      return;
    /*
     * No lock needed because we are using a util.concurrent collection.
     */
    f_observers.add(o);
  }

  public void removeObserver(ISelectionObserver o) {
    /*
     * No lock needed because we are using a util.concurrent collection.
     */
    f_observers.remove(o);
  }

  /**
   * Do not call this method holding a lock on <code>this</code>. Deadlock could
   * occur as we are invoking an alien method.
   */
  private void notifySelectionChanged() {
    for (ISelectionObserver o : f_observers)
      o.selectionChanged(this);
  }

  /**
   * Do not call this method holding a lock on <code>this</code>. Deadlock could
   * occur as we are invoking an alien method.
   */
  private void notifyColumnsChanged(Column c) {
    for (ISelectionObserver o : f_observers)
      o.columnVisibilityChanged(this, c);
  }

  @Override
  public void changed() {
    final long now = startingUpdate();
    /*
     * The database has changed. Refresh this selection if it has any filters.
     */
    final Job job = new AbstractSierraDatabaseJob("Refresh selection") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          refreshFiltersDatabaseJob(now);
          notifySelectionChanged();
        } catch (Exception e) {
          final int errNo = 53;
          final String msg = I18N.err(errNo);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * Refreshes the data within all the filters that comprise this selection.
   * <p>
   * Queries the database.
   * <p>
   * Blocks until all the queries are completed.
   */
  public void refreshFilters() {
    final long now = startingUpdate();
    final Job job = new AbstractSierraDatabaseJob("Refresh selection") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          refreshFiltersDatabaseJob(now);
        } catch (Exception e) {
          final int errNo = 53;
          final String msg = I18N.err(errNo);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
    boolean joined = false;
    while (!joined) {
      try {
        job.join();
        joined = true;
      } catch (InterruptedException e) {
        // ignore, as we'll try again if necessary.
      }
    }
  }

  private void refreshFiltersDatabaseJob(final long now) {
    synchronized (this) {
      for (Filter filter : f_filters) {
        if (continueUpdate(now)) {
          filter.refresh();
        }
      }
      finishedUpdate(now);
    }
  }

  /**
   * Invoked by a filter when the amount of findings allowed through the filter
   * changed. This would be a change that occurred in the user interface.
   * <p>
   * This method must never be called during a refresh or an infinite loop of
   * refreshes could occur.
   * 
   * @param changedFilter
   *          a filter that is part of this selection.
   */
  void filterChanged(final Filter changedFilter) {
    final Job job = new AbstractSierraDatabaseJob("Refresh filter") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          refreshFiltersAfter(changedFilter);
          notifySelectionChanged();
        } catch (Exception e) {
          final int errNo = 53;
          final String msg = I18N.err(errNo);
          return SLEclipseStatusUtility.createErrorStatus(errNo, msg, e);
        }

        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * Refreshes the data within all the filters after the passed filter.
   * <p>
   * Queries the database.
   * <p>
   * Blocks until all the queries are completed.
   */
  private void refreshFiltersAfter(Filter changedFilter) {
    /*
     * Create a work list of all the filters in this selection after the one
     * that just changed.
     */
    LinkedList<Filter> workList = new LinkedList<Filter>();
    boolean add = false;
    synchronized (this) {
      for (Filter filter : f_filters) {
        if (add) {
          workList.addLast(filter);
        } else {
          if (filter == changedFilter)
            add = true;
        }
      }
      /*
       * Do an update if the work list is not empty.
       */
      for (Filter filter : workList) {
        filter.refresh();
      }
    }
  }

  /*
   * This code was added to allow the list of findings to display columns in the
   * UI.
   */

  private final List<Column> f_listOfFindingsColumns = new CopyOnWriteArrayList<Column>();

  /**
   * Gets the column in the list of findings display (the 'Show' results) with
   * the given title.
   * 
   * @param title
   *          a column title (i.e., the name of the column).
   * @return a column or {@code null} if none can be found for the passed title.
   */
  @Nullable
  public Column getColumnByTitle(String title) {
    if (title != null)
      for (Column column : f_listOfFindingsColumns) {
        if (title.equals(column.getTitle()))
          return column;
      }
    return null;
  }

  /**
   * An alias to the columns in the list of findings display (the 'Show'
   * results). The collection returned is an alias of the collection used in the
   * implementation of this class, so changes to the list are reflected in the
   * internal collection, and vice-versa.
   * 
   * @return the list of findings columns which should not be mutated in most
   *         cases, one exception would be {@link SelectionPersistence}.
   */
  public List<Column> getColumns() {
    return f_listOfFindingsColumns;
  }

  /**
   * Changes the visibility of the column with the given name.
   * 
   * @param name
   * @param value
   *          {@code true} for the column to be visible, {@code false} for it to
   *          be invisible.
   * @return
   */
  public boolean setColumnVisible(String title, boolean value) {
    Column c = getColumnByTitle(title);
    if (c == null || c.isVisible() == value) {
      return false; // Nothing changed
    }
    c.setVisible(value);
    notifyColumnsChanged(c);
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append("[Selection filters={");
    boolean first = true;
    for (Filter f : f_filters) {
      if (first)
        first = false;
      else
        b.append(",");
      b.append(f.toString());
    }
    b.append("}]");
    return b.toString();
  }
}
