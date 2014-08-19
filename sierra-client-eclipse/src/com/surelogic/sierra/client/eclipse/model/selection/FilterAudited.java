package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;

public final class FilterAudited extends Filter {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterAudited(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Audited";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getGrayscaleImage(CommonImages.IMG_SIERRA_STAMP_SMALL);
    }
  };

  FilterAudited(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "AUDITED";
  }

  @Override
  public Image getImageFor(String value) {
    String imageName = CommonImages.IMG_UNKNOWN;
    if (YES.equals(value))
      imageName = CommonImages.IMG_SIGNED_YES;
    else if (NO.equals(value))
      imageName = CommonImages.IMG_SIGNED_NO;
    return SLImages.getImage(imageName);
  }

  private static final String YES = "Yes";
  private static final String NO = "No";

  @Override
  protected void deriveAllValues() {
    String[] values = new String[] { YES, NO };
    synchronized (this) {
      f_allValues.clear();
      f_allValues.addAll(Arrays.asList(values));
    }
  }
}
