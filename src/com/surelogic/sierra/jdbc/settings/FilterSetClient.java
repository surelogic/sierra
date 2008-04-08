package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.jdbc.DBQuery;
import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.ListFilterSetResponse;

public class FilterSetClient {

	public static final DBQuery<Void> updateFilterSets(
			final ListFilterSetResponse response) {
		return new DBQuery<Void>() {
			public Void perform(Query q) {
				final FilterSets sets = new FilterSets(q);
				for (final FilterSet set : response.getFilterSets()) {
					sets.writeFilterSet(FilterSets.convertDO(set));
				}
				return null;
			}
		};
	}

}
