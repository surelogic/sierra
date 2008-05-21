package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.IBuglinkDataObserver;
import com.surelogic.sierra.jdbc.settings.CategoryDO;

public final class FilterAdHocFindingCategory extends Filter 
implements IBuglinkDataObserver {
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			FilterAdHocFindingCategory f = new FilterAdHocFindingCategory(selection, previous);
			BuglinkData.getInstance().addObserver(f);
			return f;
		}

		public String getFilterLabel() {
			return "Adhoc Category";
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
	protected void deriveAllValues() throws Exception {	
		f_allValues.clear();
		
		final BuglinkData buglink = BuglinkData.getInstance();
		synchronized (buglink) {
			f_allValues.addAll(buglink.getCategoryUids());
			Collections.sort(f_allValues, new Comparator<String>() {
				public int compare(String o1, String o2) {
					// FIX cache one of these?
					CategoryDO cat1 = buglink.getCategoryDef(o1);
					CategoryDO cat2 = buglink.getCategoryDef(o2);
					return cat1.getName().compareTo(cat2.getName());
				}				
			});

			for(String cat : f_allValues) {
				// Compute the count for the category
				int count = 0;
				for(String findingType : buglink.getFindingTypes(cat)) {
					String typeName = buglink.getFindingType(findingType).getName();
					Integer inc = f_counts.get(typeName);
					if (inc != null) {
						count += inc;
					}
				}
				f_counts.put(cat, count);
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
    		List<String> result = new ArrayList<String>(porous.size());
    		for(String value : porous) {
    			String typeName = BuglinkData.getInstance().getFindingType(value).getName();
    			result.add(typeName);
    		}
    		return result;
    	}
    }
    
	public void notify(BuglinkData bd) {
		refresh();		
	}
    
    @Override
    void dispose() {
    	BuglinkData.getInstance().removeObserver(this);
    	super.dispose();
    }
}
