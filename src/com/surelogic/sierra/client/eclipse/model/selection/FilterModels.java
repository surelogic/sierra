package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.*;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.model.BuglinkData;

public final class FilterModels extends Filter {
	// FIX should use FINDING_ID
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
		return null; // FIX to get assurance type to show an image
	}
}
