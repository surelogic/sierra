package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.core.JDTUtility;
import com.surelogic.common.ui.SLImages;

public final class FilterJavaType extends Filter {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterJavaType(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Java Type";
    }
  };

  FilterJavaType(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "CLASS";
  }

  @Override
  public Image getImageFor(String value) {
    final IType jdtType = JDTUtility.findIType(value);
    if (jdtType == null)
      return SLImages.getImage(CommonImages.IMG_CLASS);
    else
      return SLImages.getImageFor(jdtType);
  }
}
