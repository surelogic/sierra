package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.SierraUIUtility;

public final class FilterTool extends Filter {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterTool(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Tool";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getGrayscaleImage(CommonImages.IMG_SIERRA_LOGO);
    }
  };

  FilterTool(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "TOOL";
  }

  @Override
  public Image getImageFor(String value) {
    return SierraUIUtility.getImageForTool(value);
  }
}
