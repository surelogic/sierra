package com.surelogic.sierra.client.eclipse.model.selection;

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
  private final IColumnCellProvider f_cellProvider;

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
  private ColumnSort f_sort = ColumnSort.UNSORTED;

  /**
   * Flag if the column is visible.
   */
  @InRegion("ColumnState")
  private boolean f_visible = false;

  @InRegion("ColumnState")
  private int f_width = -1;

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
    f_visible = toCopy.f_visible;
    f_width = toCopy.f_width;
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

  public synchronized final int getIndex() {
    return f_index;
  }

  public synchronized void setIndex(final int value) {
    if (value < 0)
      throw new IllegalArgumentException("illegal index: " + value);
    f_index = value;
  }

  public synchronized final ColumnSort getSort() {
    return f_sort;
  }

  public synchronized void setSort(final ColumnSort value) {
    if (value == null)
      throw new IllegalArgumentException(I18N.err(44, "value"));
    f_sort = value;
  }

  public synchronized final int getWidth() {
    return f_width;
  }

  public synchronized void setWidth(final int value) {
    if (value < -1) {
      throw new IllegalArgumentException("illegal column width: " + value);
    }
    f_width = value;
  }

  /**
   * Gets if this column is visible in the user interface.
   * 
   * @return {@code true} for this column to be visible, {@code false} if it is
   *         invisible.
   */
  public synchronized final boolean isVisible() {
    return f_visible;
  }

  /**
   * Use {@link Selection#setColumnVisible(String, boolean)}, never call this
   * method.
   * 
   * @param value
   *          {@code true} for this column to be visible, {@code false} for it
   *          to be invisible.
   */
  synchronized void setVisible(boolean value) {
    f_visible = value;
  }
}
