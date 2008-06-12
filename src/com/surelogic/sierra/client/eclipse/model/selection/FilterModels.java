package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.jdbc.settings.CategoryDO;


public final class FilterModels extends Filter {
	private static final String COLUMN_NAME = "SUMMARY"; // For the raw data
	private static final String MODEL_CATEGORY_ID = "00000006-ef51-4f9c-92f6-351d214f46e7";
	
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterModels(selection, previous);
		}

		public String getFilterLabel() {
			return "JSure Models";
		}
	};

	FilterModels(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}
	
	@Override
	protected String getColumnName() {
		return COLUMN_NAME;
	}
	
	@Override
	protected String getMinimalWhereClausePart() {
		return createInClause("FINDING_TYPE", 
				              BuglinkData.getInstance().getFindingTypes(MODEL_CATEGORY_ID));				
	}
	
	/*
	@Override
	protected void deriveAllValues() {
		synchronized (this) {
			f_allValues.clear();
			f_allValues.add(FINDING);
			f_allValues.add(VERIFICATION);
			
			int verifications = getSummaryCountFor(FilterVerificationStatus.CONSISTENT) +
			                    getSummaryCountFor(FilterVerificationStatus.INCONSISTENT);
			f_counts.put(VERIFICATION, verifications);
			f_counts.put(FINDING, f_counts.get(null));		
		}
	}
	
    @Override
    protected Iterable<String> getMappedPorousValues() {
    	Set<String> porous = new HashSet<String>();
    	if (f_porousValues.contains(FINDING)) {
    		porous.add(null);
    	}
    	if (f_porousValues.contains(VERIFICATION)) {
    		porous.add(FilterVerificationStatus.CONSISTENT);
    		porous.add(FilterVerificationStatus.INCONSISTENT);
    	}
    	return porous;
    }
    */
}
