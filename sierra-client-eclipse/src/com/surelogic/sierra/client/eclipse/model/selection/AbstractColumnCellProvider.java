package com.surelogic.sierra.client.eclipse.model.selection;

import com.surelogic.common.StringComparators;

/**
 * A default implementation of the
 * {@link #compareInternal(Column, FindingData, FindingData)} method is provided
 * that does a sort based upon the label provided for the cell to the user
 * interface. This may be used for columns that display text.
 */
public abstract class AbstractColumnCellProvider implements IColumnCellProvider {

  @Override
  public int compareInternal(Column column, FindingData row1, FindingData row2) {
    return StringComparators.SORT_ALPHABETICALLY.compare(column.getCellProvider().getLabel(row1), column.getCellProvider()
        .getLabel(row2));
  }

}
