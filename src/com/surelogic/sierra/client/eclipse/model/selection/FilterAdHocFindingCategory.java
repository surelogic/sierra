package com.surelogic.sierra.client.eclipse.model.selection;

import java.sql.*;
import java.util.*;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.settings.*;
import com.surelogic.sierra.jdbc.tool.*;

public final class FilterAdHocFindingCategory extends Filter {
	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterAdHocFindingCategory(selection, previous);
		}

		public String getFilterLabel() {
			return "Adhoc Category";
		}
	};

	private final Map<String,Set<String>> f_categories = new HashMap<String,Set<String>>();
	private final Map<String,FindingTypeDO> f_findingTypes = 
		new HashMap<String,FindingTypeDO>();
	
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
		Connection conn = Data.readOnlyConnection();
		try {
			ConnectionQuery q = new ConnectionQuery(conn);
			FindingTypes ft = new FindingTypes(q);
			List<FindingTypeDO> ftypes = ft.listFindingTypes();
			f_findingTypes.clear();
			for(FindingTypeDO ftype : ftypes) {
				f_findingTypes.put(ftype.getUid(), ftype);
			}
			
			List<CategoryDO> raw = Data.withReadOnly(SettingQueries.getLocalCategories());	
			Map<String,CategoryDO> categories = new HashMap<String, CategoryDO>(raw.size());
			f_allValues.clear();

			for(CategoryDO c : raw) {
				categories.put(c.getName(), c);
				f_allValues.add(c.getName());
			}
			Collections.sort(f_allValues);

			f_categories.clear();
			for(String cat : f_allValues) {
				computeCategory(categories, cat);
			}
		} finally {
			conn.close();
		}
	}

	/**
	 * Computes the finding types in each category, as well as
	 * the finding count	 
	 */
	private Set<String> computeCategory(Map<String,CategoryDO> categories, String name) {
		Set<String> categoryTypes = f_categories.get(name);
		if (categoryTypes == null) {
		    // Compute which finding types are in the category
			CategoryDO defn = categories.get(name);
			categoryTypes = new HashSet<String>();
			for(String parent : defn.getParents()) {
				categoryTypes.addAll(computeCategory(categories, parent));
			}
			for(CategoryEntryDO filter : defn.getFilters()) {				
				if (filter.isFiltered()) {
					categoryTypes.remove(filter.getFindingType());
				} else {
					categoryTypes.add(filter.getFindingType());
				}
			}
			f_categories.put(name, categoryTypes);
			
		    // Compute the count for the category
			int count = 0;
			for(String findingType : categoryTypes) {
				String typeName = f_findingTypes.get(findingType).getName();
				Integer inc = f_counts.get(typeName);
				if (inc != null) {
					count += inc;
				}
			}
			Integer lastCount = f_counts.put(name, count);
			if (lastCount != null) {
				SLLogger.getLogger().severe("Duplicate count for category "+name);
			}
		}
		return categoryTypes;
	}

    @Override
    protected Iterable<String> getMappedPorousValues() {
    	Set<String> porous = new HashSet<String>();
		for (String value : f_porousValues) {
			porous.addAll(f_categories.get(value));
		}
		List<String> result = new ArrayList<String>(porous.size());
		for(String value : porous) {
			String typeName = f_findingTypes.get(value).getName();
			result.add(typeName);
		}
		return result;
    }
}
