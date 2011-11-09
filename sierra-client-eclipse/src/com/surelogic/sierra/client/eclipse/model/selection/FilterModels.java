package com.surelogic.sierra.client.eclipse.model.selection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.jsure.*;
import com.surelogic.sierra.client.eclipse.model.BuglinkData;
import com.surelogic.sierra.tool.message.AssuranceType;

public final class FilterModels extends Filter {
	private static final String COLUMN_NAME = "FO.FINDING_ID"; // For the raw data
	
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
	protected final Map<String,String> f_summaries = new HashMap<String,String>();
	
	FilterModels(Selection selection, Filter previous) {
		super(selection, previous);
		f_quote = false;
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
		return createInClause(true, "FO.FINDING_TYPE", 
				              BuglinkData.getInstance().getFindingTypes(JSureUtil.MODEL_CATEGORY_ID));				
	}
	
	private final int MAX_TOKENS = 2;
	
	@Override
	public String getLabel(String id) {
		String completeMsg = f_summaries.get(id);
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
	protected void sortAllValues() {
		Collections.sort(f_allValues, new Comparator<String>() {
			public int compare(String id1, String id2) {
				return f_summaries.get(id1).compareTo(f_summaries.get(id2));
			}
		});
	}
	
	@Override
	public Image getImageFor(String value) {
		return f_images.get(value);
	}
	
	@Override
	protected void grabExtraCountsData(String value, ResultSet rs) throws SQLException {
		final String aType         = rs.getString(3);
		final String findingTypeId = rs.getString(4);
		final String summary       = rs.getString(5);
		final Image image          = JSureUtil.getImageFor(findingTypeId, 
				                                           AssuranceType.fromFlag(aType));
		f_images.put(value, image);
		f_summaries.put(value, summary);		
	}
	
	@Override
	protected String getBaseCountsQuery() {
		return "FindingsSelectionView.countModels";
	}
	
	static final String nestedTables = "from FINDING_RELATION_OVERVIEW FRO where";
	
	@Override
	protected String getJoinPart() {		
		return "where (FJ.FINDING_ID in (" +
		         "(select FRO.PARENT_FINDING_ID "+nestedTables+" FRO.CHILD_FINDING_ID = FO.FINDING_ID)" +
		         " union all " +
		         "(select FRO.CHILD_FINDING_ID "+nestedTables+" FRO.PARENT_FINDING_ID = FO.FINDING_ID)" +
		         ") or FO.FINDING_ID = FJ.FINDING_ID)";		
	  /*
	  return ", FINDING_RELATION_OVERVIEW FRO where (" +
             //"(FO.FINDING_ID = FJ.FINDING_ID) OR " +
	         //"(FO.FINDING_ID = FRO.PARENT_FINDING_ID AND FRO.CHILD_FINDING_ID = FJ.FINDING_ID) OR " +
	         "(FO.FINDING_ID = FRO.CHILD_FINDING_ID AND FRO.PARENT_FINDING_ID = FJ.FINDING_ID))";	  
	  */
	}
	
	@Override
	public boolean selfUsesJoin() {
		return true;
	}
}
