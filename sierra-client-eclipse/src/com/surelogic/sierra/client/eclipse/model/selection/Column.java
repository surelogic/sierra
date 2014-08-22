package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;

import com.surelogic.InRegion;
import com.surelogic.NonNull;
import com.surelogic.Region;
import com.surelogic.RegionLock;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

/**
 * A column in the list of findings table (the 'Show' result).
 */
@ThreadSafe
@Region("private ColumnState")
@RegionLock("ColumnLock is this protects ColumnState")
public final class Column {

  public static final String SUMMARY_COLUMN = "Summary";
  public static final String TOOL_COLUMN = "Tool";
  public static final String PROJECT_COLUMN = "Project";
  public static final String PACKAGE_COLUMN = "Package";
  public static final String TYPE_COLUMN = "Type";
  public static final String LINE_COLUMN = "Line";
  public static final String IMPORTANCE_COLUMN = "Importance";
  public static final String FINDING_TYPE_COLUMN = "Finding Type";

  public static final String[] COLUMNS = { SUMMARY_COLUMN, TOOL_COLUMN, PROJECT_COLUMN, PACKAGE_COLUMN, TYPE_COLUMN, LINE_COLUMN,
      IMPORTANCE_COLUMN, FINDING_TYPE_COLUMN };

  /*
   * immutable state
   */

  /**
   * The title of the column.
   */
  @NonNull
  private final String f_title;

  /**
   * Allows this column to use a row's {@link FindingData} to display a label
   * and image.
   */
  @NonNull
  final IColumnCellProvider f_cellProvider;

  /**
   * he comparator for this column using its associated cell provider to be used
   * for sorting.
   */
  @NonNull
  private final Comparator<FindingData> f_findingComparator;

  /**
   * One of <tt>SWT.LEFT</tt>, <tt>SWT.RIGHT</tt>, <tt>SWT.CENTER</tt>.
   */
  private final int f_swtAlignment;

  /*
   * mutable state protected by a lock on the class
   */

  /**
   * The position of this column, left to right, counting up from zero.
   */
  @InRegion("ColumnState")
  private int f_index;

  /**
   * Model of how a column is intended to be sorted.
   */
  @InRegion("ColumnState")
  @NonNull
  ColumnSort f_sort = ColumnSort.UNSORTED;

  /**
   * The user set width for this column in pixels. A value of -1 indicates that
   * no preference has been set and the width should be calculated on the fly.
   */
  @InRegion("ColumnState")
  private int f_userSetWidth = -1;

  @Override
  public String toString() {
    return getClass().getName() + " : " + f_title;
  }

  /**
   * Defaults to a column alignment of <tt>SWT.LEFT</tt>.
   * 
   * @param title
   *          of the column.
   * @param cellProvider
   *          how to display a finding in this column.
   */
  public Column(String title, IColumnCellProvider cellProvider) {
    this(title, cellProvider, SWT.LEFT);
  }

  /**
   * Constructs an instance of a column.
   * 
   * @param title
   *          of the column.
   * @param cellProvider
   *          how to display a finding in this column.
   * @param swtAlignment
   *          the alignment to be used for the SWT table this column is
   *          displayed within, one of <tt>SWT.LEFT</tt>, <tt>SWT.RIGHT</tt>,
   *          <tt>SWT.CENTER</tt>.
   */
  public Column(String title, IColumnCellProvider cellProvider, int swtAlignment) {
    if (title == null)
      throw new IllegalArgumentException(I18N.err(44, "title"));
    f_title = title;
    if (cellProvider == null)
      throw new IllegalArgumentException(I18N.err(44, "cellProvider"));
    f_cellProvider = cellProvider;
    f_findingComparator = new Comparator<FindingData>() {
      @Override
      public int compare(final FindingData o1, final FindingData o2) {
        return f_sort == ColumnSort.SORT_DOWN ? -f_cellProvider.compareInternal(Column.this, o1, o2) : f_cellProvider
            .compareInternal(Column.this, o1, o2);
      }
    };
    f_swtAlignment = swtAlignment;
  }

  /**
   * Copy constructor.
   * 
   * @param toCopy
   *          another column.
   */
  public Column(Column toCopy) {
    this(toCopy.f_title, toCopy.f_cellProvider, toCopy.f_swtAlignment);
    f_index = toCopy.f_index;
    f_sort = toCopy.f_sort;
    f_userSetWidth = toCopy.f_userSetWidth;
  }

  /*
   * Immutable getters (don't need lock)
   */

  /**
   * Gets the column title.
   * 
   * @return a title.
   */
  @NonNull
  public String getTitle() {
    return f_title;
  }

  /**
   * Gets this column's cell provider.
   * 
   * @return this column's cell provider.
   */
  @NonNull
  public IColumnCellProvider getCellProvider() {
    return f_cellProvider;
  }

  /**
   * Gets the alignment to be used for the SWT table this column is displayed
   * within.
   * 
   * @return One of <tt>SWT.LEFT</tt>, <tt>SWT.RIGHT</tt>, <tt>SWT.CENTER</tt>.
   */
  public int getSwtAlignment() {
    return f_swtAlignment;
  }

  /**
   * Gets the comparator for this column using its associated cell provider to
   * be used for sorting.
   * 
   * @return a finding comparator.
   */
  @NonNull
  public Comparator<FindingData> getFindingComparator() {
    return f_findingComparator;
  }

  /*
   * Getters and setters for mutable state (do need lock)
   */

  /**
   * Resets this column's mutable information to the correct default for table
   * display.
   */
  public synchronized final void reset() {
    f_index = getDefaultIndex();
    f_userSetWidth = -1;
    // by default the first column is sorted the others unsorted
    f_sort = getDefaultSort();
  }

  /**
   * Checks is this column's mutable information has changes to the default
   * values.
   * <p>
   * Used to determine if any user changes need to be persisted across Eclipse
   * sessions.
   * 
   * @return {@code true} if this column's mutable information does not have
   *         default values, {@code false} otherwise (all default).
   */
  public synchronized boolean isDirty() {
    // column width
    if (f_userSetWidth != -1)
      return true;

    // column position
    if (f_index != getDefaultIndex())
      return true;

    // sort
    if (f_sort != getDefaultSort())
      return true;

    return false;
  }

  /**
   * Gets the index of this column in the displayed table. The user may change
   * the column display order.
   * 
   * @return the index of this column in the displayed table.
   */
  public synchronized final int getIndex() {
    return f_index;
  }

  /**
   * Sets the index of this column in the displayed table. The user may change
   * the column display order.
   * 
   * @param value
   *          the index of this column in the displayed table.
   * 
   * @throws IllegalArgumentException
   *           if the value is less than zero (but does not check that all the
   *           columns are consistent.
   */
  public synchronized void setIndex(final int value) {
    if (value < 0)
      throw new IllegalArgumentException("illegal index: " + value);
    f_index = value;
  }

  /**
   * Gets the default index value for this column based upon its title.
   * 
   * @return the default index value for this column based upon its title.
   */
  public synchronized int getDefaultIndex() {
    for (int i = 0; i < COLUMNS.length; i++)
      if (COLUMNS[i].equals(f_title)) {
        return i;
      }
    throw new IllegalStateException("Column title " + f_title + "is unknown, not one of: " + Arrays.toString(COLUMNS));
  }

  /**
   * Gets if and how the column contributes to the sorting of the table.
   * 
   * @return if and how column contributes to the sorting of the table.
   */
  public synchronized final ColumnSort getSort() {
    return f_sort;
  }

  /**
   * Sets if and how the column contributes to the sorting of the table.
   * 
   * @param value
   *          if and how the column contributes to the sorting of the table.
   * 
   * @throws IllegalArgumentException
   *           if value is null.
   */
  public synchronized void setSort(@NonNull final ColumnSort value) {
    if (value == null)
      throw new IllegalArgumentException(I18N.err(44, "value"));
    f_sort = value;
  }

  /**
   * Gets the default sort value for this column based upon its title. The first
   * column is sorted the others unsorted.
   * 
   * @return the default sort value for this column based upon its title.
   */
  public synchronized ColumnSort getDefaultSort() {
    return f_title.equals(SUMMARY_COLUMN) ? ColumnSort.SORT_UP : ColumnSort.UNSORTED;
  }

  /**
   * The value of the width the user has set for this column. A value of -1
   * indicates that no preference has been set and the width should be
   * calculated on the fly.
   * 
   * @return a width in pixels.
   */
  public synchronized final int getUserSetWidth() {
    return f_userSetWidth;
  }

  /**
   * Gets if the user has set a column width.
   * 
   * @return {@code true} if the user has set a column width, {@code false}
   *         otherwise.
   */
  public synchronized boolean hasUserSetWidth() {
    return f_userSetWidth != -1;
  }

  /**
   * 
   * @param value
   *          a width in pixels or <tt>-1</tt> to clear the width preference.
   */
  public synchronized void setUserSetWidth(final int value) {
    if (value < -1) {
      throw new IllegalArgumentException("illegal column width: " + value);
    }
    f_userSetWidth = value;
  }

}
