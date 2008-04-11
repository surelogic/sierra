package com.surelogic.sierra.jdbc.settings;

import com.surelogic.sierra.jdbc.DBQuery;
import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Queryable;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.ListCategoryRequest;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * This class represents implementations of the client-side behavior of the bug
 * link service.
 * 
 * @author nathan
 * 
 */
public class BugLinkClient {

	/**
	 * Queries the specified server for a list of categories, and returns a
	 * {@link DBQuery} that, when run, will write the given categories into the
	 * database. Categories that are of a lower revision than the local copy
	 * will not be overwritten.
	 * 
	 * @param loc
	 * @return
	 */
	public static final DBQuery<ListCategoryResponse> updateCategories(
			SierraServerLocation loc) {
		final ListCategoryResponse response = BugLinkServiceClient.create(loc)
				.listCategories(new ListCategoryRequest());
		return new DBQuery<ListCategoryResponse>() {
			public ListCategoryResponse perform(Query q) {
				final Categories sets = new Categories(q);
				final Queryable<Void> delete = q
						.prepared("Definitions.deleteDefinition");
				final Queryable<Void> insert = q
						.prepared("Definitions.insertDefinition");
				for (final FilterSet set : response.getFilterSets()) {
					final String uid = set.getUid();
					final CategoryDO c = sets.getCategory(uid);
					if ((c != null) && (c.getRevision() < set.getRevision())) {
						delete.call(uid);
						insert.call(uid, set.getOwner());
						sets.writeCategory(Categories.convertDO(set));
					}
				}
				return response;
			}
		};
	}

}
