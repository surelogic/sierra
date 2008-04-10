package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.jdbc.DBQuery;
import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.ListCategoryRequest;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class ScanFilterClient {

	public static final DBQuery<Void> updateFilterSets(SierraServerLocation loc) {
		final ListCategoryResponse response = BugLinkServiceClient.create(loc)
				.listCategories(new ListCategoryRequest());
		return new DBQuery<Void>() {
			public Void perform(Query q) {
				final Categories sets = new Categories(q);
				for (final FilterSet set : response.getFilterSets()) {
					sets.writeCategory(Categories.convertDO(set));
				}
				return null;
			}
		};
	}

}
