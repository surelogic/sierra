package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Collections;
import java.util.Comparator;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;

public final class FilterFindingType extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterFindingType(selection, previous);
		}

		public String getFilterLabel() {
			return "Finding Type";
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
		return "FO.FINDING_TYPE";
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
			public int compare(String o1, String o2) {
				// FIX cache one of these?
				FindingTypeDO def1 = buglink.getFindingType(o1);
				FindingTypeDO def2 = buglink.getFindingType(o2);
				return def1.getName().compareTo(def2.getName());
			}				
		});
	}
}
