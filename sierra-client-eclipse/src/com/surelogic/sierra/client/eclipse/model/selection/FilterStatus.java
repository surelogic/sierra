package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Set;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;

public final class FilterStatus extends Filter {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterStatus(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Status";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getImage(CommonImages.IMG_TASK_NEW);
    }

    @Override
    public boolean addWhereClauseIfUnusedFilter(Set<ISelectionFilterFactory> unused, StringBuilder b, boolean first,
        boolean usesJoin) {
      first = addClausePrefix(b, first);
      addWhereClauseToFilterOutFixed(b, usesJoin);
      return first;
    }
  };

  FilterStatus(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  private static final String COLUMN_NAME = "STATUS";

  @Override
  protected String getColumnName() {
    return COLUMN_NAME;
  }

  @Override
  public Image getImageFor(String value) {
    return getImageHelper(value);
  }

  private static final String FIXED = "Fixed";
  private static final String NEW = "New";
  private static final String UNCHANGED = "Unchanged";

  @Override
  protected void deriveAllValues() {
    synchronized (this) {
      f_allValues.clear();
      f_allValues.add(NEW);
      f_allValues.add(UNCHANGED);
      f_allValues.add(FIXED);
    }
  }

  static Image getImageHelper(String value) {
    String imageName = CommonImages.IMG_UNKNOWN;
    if (NEW.equals(value)) {
      imageName = CommonImages.IMG_TASK_NEW;
    } else if (UNCHANGED.equals(value)) {
      imageName = CommonImages.IMG_TASK_UNCHANGED;
    } else if (FIXED.equals(value)) {
      imageName = CommonImages.IMG_TASK_COMPLETE;
    }
    return SLImages.getGrayscaleImage(imageName);
  }

  static void addWhereClauseToFilterOutFixed(StringBuilder b, boolean usesJoin) {
    if (usesJoin) {
      b.append(getTablePrefix(true));
      b.append(COLUMN_NAME + " != '" + FIXED + "'");
      b.append(" AND ");
    }
    b.append(getTablePrefix(false));
    b.append(COLUMN_NAME + " != '" + FIXED + "'");
  }
}
