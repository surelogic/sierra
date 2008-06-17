package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Set;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.images.CommonImages;
import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.tool.message.AssuranceType;

public final class FilterVerificationStatus extends Filter {
	private static final String COLUMN_NAME = "ASSURANCE_TYPE";
	public static final String CONSISTENT = "C";
	public static final String INCONSISTENT = "I";
	
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterVerificationStatus(selection, previous);
		}

		public String getFilterLabel() {
			return "Verification Status";
		}
		
		@Override
		public boolean addWhereClauseIfUnusedFilter(Set<ISelectionFilterFactory> unused,
				                                    StringBuilder b, boolean first,
				                                    boolean usesJoin) {
			if (unused.contains(FilterResultType.FACTORY) &&
		        unused.contains(FilterModels.FACTORY)) { // all unused
				first = addClausePrefix(b, first);
				b.append(getTablePrefix(usesJoin));
				b.append(COLUMN_NAME + " is NULL");
			}
			return first;
		}
	};

	FilterVerificationStatus(Selection selection, Filter previous) {
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
	protected void deriveAllValues() {
		synchronized (this) {
			f_allValues.clear();
			f_allValues.add(CONSISTENT);
			f_allValues.add(INCONSISTENT);
			f_allValues.add(null);
		}
	}
	
	@Override
	public Image getImageFor(String value) {
		AssuranceType aType = AssuranceType.fromFlag(value);
		if (aType == null) {
			return SLImages.getImage(CommonImages.IMG_ASTERISK_ORANGE_50);
		}
		return Utility.getImageFor(aType);
	}
	
	@Override
	public String getLabel(String initial) {
		if (CONSISTENT.equals(initial)) {
			return "Consistent";
		}
		if (INCONSISTENT.equals(initial)) {
			return "Inconsistent";
		}
		return "n/a";
	}	
}
