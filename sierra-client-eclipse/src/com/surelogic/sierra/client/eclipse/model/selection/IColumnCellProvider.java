package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.swt.graphics.Image;

import com.surelogic.NonNull;
import com.surelogic.Nullable;

/**
 * Implemented to provide a {@link Column} with the ability to use a row's
 * {@link FindingData} to display a label and image.
 * <p>
 * Most columns with alphabetical sorting can extend
 * {@link AbstractColumnCellProvider} rather than implement this interface
 * directly.
 * 
 * @see AbstractColumnCellProvider
 */
public interface IColumnCellProvider {

  /**
   * Gets the label for this column and the passed finding.
   * 
   * @param row
   *          a particular finding.
   * @return the label for this column and the passed data.
   */
  @NonNull
  String getLabel(FindingData row);

  /**
   * Gets the image for this column and the passed finding.
   * 
   * @param row
   *          a particular finding.
   * @return the image for this column and the passed data, or {@code null} if
   *         no image should be displayed.
   */
  @Nullable
  Image getImage(FindingData row);

  /**
   * Compares its third and forth arguments for order. Returns a negative
   * integer, zero, or a positive integer as the first argument is less than,
   * equal to, or greater than the second.
   * 
   * @param column
   *          the column information.
   * @param row1
   *          a particular finding.
   * @param row2
   *          a particular finding.
   * @return a negative integer, zero, or a positive integer as the second
   *         argument is less than, equal to, or greater than the third.
   */
  int compareInternal(Column column, FindingData row1, FindingData row2);
}
