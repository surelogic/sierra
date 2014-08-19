package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.ui.SLImages;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.IBuglinkDataObserver;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;

public final class FilterAdHocFindingCategory extends Filter implements IBuglinkDataObserver {
  public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
    @Override
    public Filter construct(Selection selection, Filter previous) {
      FilterAdHocFindingCategory f = new FilterAdHocFindingCategory(selection, previous);
      BuglinkData.getInstance().addObserver(f);
      return f;
    }

    @Override
    public String getFilterLabel() {
      return "Finding Category";
    }

    @Override
    public Image getFilterImage() {
      return SLImages.getImage(CommonImages.IMG_CATEGORY);
    }
  };

  FilterAdHocFindingCategory(Selection selection, Filter previous) {
    super(selection, previous);
  }

  @Override
  public ISelectionFilterFactory getFactory() {
    return FACTORY;
  }

  @Override
  protected String getColumnName() {
    return "FINDING_TYPE"; // For the raw data
  }

  @Override
  public String getLabel(String uid) {
    CategoryDO cat = BuglinkData.getInstance().getCategoryDef(uid);
    return cat.getName();
  }

  @Override
  public Image getImageFor(String value) {
    return SLImages.getGrayscaleImage(CommonImages.IMG_CATEGORY);
  }

  @Override
  protected void deriveAllValues() throws Exception {
    f_allValues.clear();

    final BuglinkData buglink = BuglinkData.getInstance();
    synchronized (buglink) {
      f_allValues.addAll(buglink.getCategoryUids());
      Collections.sort(f_allValues, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          // FIX cache one of these?
          CategoryDO cat1 = buglink.getCategoryDef(o1);
          CategoryDO cat2 = buglink.getCategoryDef(o2);
          return cat1.getName().compareTo(cat2.getName());
        }
      });

      final Iterator<String> it = f_allValues.iterator();
      while (it.hasNext()) {
        final String cat = it.next();
        // final CategoryDO catDef = buglink.getCategoryDef(cat);

        // Compute the count for the category
        int count = 0;
        for (String findingType : buglink.getFindingTypes(cat)) {
          final FindingTypeDO def = buglink.getFindingType(findingType);
          final String typeId = def.getUid();
          // final String typeName = def.getName();
          Integer inc = f_counts.get(typeId);
          if (inc != null) {
            // System.out.println(catDef.getName()+" :
            // "+def.getName()+" - "+typeId+" = "+inc);
            count += inc;
          }
        }
        if (count == 0) {
          it.remove();
        } else {
          f_counts.put(cat, count);
        }
      }
    }
  }

  @Override
  protected Iterable<String> getMappedPorousValues() {
    Set<String> porous = new HashSet<String>();

    synchronized (BuglinkData.getInstance()) {
      for (String value : f_porousValues) {
        Collection<String> types = BuglinkData.getInstance().getFindingTypes(value);
        if (types != null) {
          porous.addAll(types);
        }
      }
      /*
       * List<String> result = new ArrayList<String>(porous.size()); for(String
       * value : porous) { String typeName =
       * BuglinkData.getInstance().getFindingType(value).getName();
       * result.add(typeName); }
       */
      return porous;
    }
  }

  @Override
  public void notify(BuglinkData bd) {
    refresh();
  }

  @Override
  void dispose() {
    BuglinkData.getInstance().removeObserver(this);
    super.dispose();
  }
}
