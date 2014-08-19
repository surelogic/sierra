package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;

public final class FilterFindingType extends Filter {

  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      return new FilterFindingType(selection, previous);
    }

    @Override
    public String getFilterLabel() {
      return "Finding Type";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getGrayscaleImage(CommonImages.IMG_INDEX_CARD);
    }
  };

  FilterFindingType(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "FINDING_TYPE";
  }

  @Override
  public String getLabel(String uid) {
    final FindingTypeDO def = BuglinkData.getInstance().getFindingType(uid);
    return def.getName();
  }

  @Override
  protected void deriveAllValues() throws Exception {
    f_allValues.clear();
    f_allValues.addAll(f_counts.keySet());

    final BuglinkData buglink = BuglinkData.getInstance();
    Collections.sort(f_allValues, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        // FIX cache one of these?
        FindingTypeDO def1 = buglink.getFindingType(o1);
        FindingTypeDO def2 = buglink.getFindingType(o2);
        return def1.getName().compareTo(def2.getName());
      }
    });
  }
}
