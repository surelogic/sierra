package com.surelogic.sierra.client.eclipse.model.selection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;

public final class FilterArtifactCount extends FilterNumberValue {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterArtifactCount(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Tool Artifacts";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getDecoratedImage(SLImages.getGrayscaleImage(CommonImages.IMG_SIERRA_LOGO), new ImageDescriptor[] { null,
          SLImages.getImageDescriptor(CommonImages.DECR_COUNT), null, null, null });
    }
  };

  FilterArtifactCount(Selection selection, Filter previous) {
    super(selection, previous);
    f_quote = false;
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "ARTIFACT_COUNT";
  }
}
