package com.surelogic.sierra.client.eclipse.model.selection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.jsure.*;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.tool.message.AssuranceType;

public final class FilterModels extends Filter {
	// FIX should use FINDING_ID
	private static final String COLUMN_NAME = "FO.SUMMARY"; // For the raw data
	
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterModels(selection, previous);
		}

		public String getFilterLabel() {
			return "JSure Models";
		}
	};

	/**
	 * Images for just this filter. Only mutated by {@link #refresh()}.
	 */
	protected final Map<String,Image> f_images = new HashMap<String,Image>();
	
	
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
		return createInClause("FO.FINDING_TYPE", 
				              BuglinkData.getInstance().getFindingTypes(JSureUtil.MODEL_CATEGORY_ID));				
	}
	
	private final int MAX_TOKENS = 2;
	
	@Override
	public String getLabel(String completeMsg) {
		StringBuilder b = new StringBuilder();
		StringTokenizer st = new StringTokenizer(completeMsg);
		for(int i = 0; st.hasMoreTokens() && i<MAX_TOKENS; i++) {
			String token = st.nextToken();
			if (i != 0) {
				b.append(' ');
			}
			b.append(token);
		}		
		return b.toString();
	}
	
	@Override
	public Image getImageFor(String value) {
		return f_images.get(value);
	}
	
	@Override
	protected void grabExtraCountsData(String value, ResultSet rs) throws SQLException {
		final String aType         = rs.getString(3);
		final String findingTypeId = rs.getString(4);
		final Image image          = JSureUtil.getImageFor(findingTypeId, 
				                                           AssuranceType.fromFlag(aType));
		f_images.put(value, image);
	}
	
	@Override
	protected String getBaseCountsQuery() {
		return "FindingsSelectionView.countModels";
	}
	
	@Override
	protected String getJoinPart() {
	  /*
	  return ", FINDING_RELATION_OVERVIEW FRO where (" +
	         "(FO.FINDING_ID = FRO.PARENT_FINDING_ID AND FRO.CHILD_FINDING_ID = FJ.FINDING_ID) OR " +
	         "(FO.FINDING_ID = FRO.CHILD_FINDING_ID AND FRO.PARENT_FINDING_ID = FJ.FINDING_ID))";
	  */
	  return ", FINDING_RELATION_OVERVIEW FRO where "+ 
	         "(FO.FINDING_ID = FRO.CHILD_FINDING_ID AND FRO.PARENT_FINDING_ID = FJ.FINDING_ID)";
	}
	
	@Override
	public boolean selfUsesJoin() {
		return true;
	}
}
