package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.client.eclipse.model.IBuglinkDataObserver;

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
	protected void deriveAllValues() throws Exception {	
		f_allValues.clear();
		
		synchronized (BuglinkData.getInstance()) {
			f_allValues.addAll(BuglinkData.getInstance().getCategoryNames());
			Collections.sort(f_allValues);

			for(String cat : f_allValues) {
				// Compute the count for the category
				int count = 0;
				for(String findingType : BuglinkData.getInstance().getFindingTypes(cat)) {
					String typeName = BuglinkData.getInstance().getFindingType(findingType).getName();
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
    			porous.addAll(BuglinkData.getInstance().getFindingTypes(value));
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
