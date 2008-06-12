package com.surelogic.sierra.client.eclipse.model.selection;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
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
		return "FINDING_TYPE";
	}
	
	@Override
	public String getLabel(String uid) {
		final FindingTypeDO def = BuglinkData.getInstance().getFindingType(uid);
		return def.getName();
	}
}
