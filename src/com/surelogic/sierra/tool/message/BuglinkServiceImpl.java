package com.surelogic.sierra.tool.message;

import java.util.List;
import java.util.Set;

import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.settings.FilterEntryDO;
import com.surelogic.sierra.jdbc.settings.FilterSetDO;
import com.surelogic.sierra.jdbc.settings.FilterSets;

public class BuglinkServiceImpl extends SierraServiceImpl implements
		BugLinkService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8719740627758175475L;

	public CreateFilterSetResponse createFilterSet(
			final CreateFilterSetRequest request) {
		return ConnectionFactory
				.withTransaction(new ServerQuery<CreateFilterSetResponse>() {
					public CreateFilterSetResponse perform(Query q, Server s) {
						final FilterSets sets = new FilterSets(q);
						final long revision = s.nextRevision();
						final FilterSetDO set = sets.createFilterSet(request
								.getName(), request.getDescription(), revision);
						final Set<FilterEntryDO> entries = set.getFilters();
						for (final FilterEntry e : request.getFilter()) {
							entries.add(new FilterEntryDO(e.getType(), e
									.isFiltered()));
						}
						final Set<String> parents = set.getParents();
						for (final String p : request.getParent()) {
							parents.add(p);
						}
						final CreateFilterSetResponse response = new CreateFilterSetResponse();
						response.setSet(FilterSets.convert(sets
								.updateFilterSet(set, revision), s.getUid()));
						return response;
					}
				});
	}

	public ListFilterSetResponse listFilterSets(ListFilterSetRequest request) {
		return ConnectionFactory
				.withReadOnly(new ServerQuery<ListFilterSetResponse>() {

					public ListFilterSetResponse perform(Query q, Server s) {
						final ListFilterSetResponse response = new ListFilterSetResponse();
						final String server = s.getUid();
						final List<FilterSet> sets = response.getFilterSets();
						for (final FilterSetDO set : new FilterSets(q)
								.listFilterSets()) {
							sets.add(FilterSets.convert(set, server));
						}
						return response;
					}
				});
	}

	public UpdateFilterSetResponse updateFilterSet(
			final UpdateFilterSetRequest request) throws RevisionException {
		return ConnectionFactory
				.withTransaction(new ServerQuery<UpdateFilterSetResponse>() {
					public UpdateFilterSetResponse perform(Query q, Server s) {
						final UpdateFilterSetResponse response = new UpdateFilterSetResponse();
						response.setSet(FilterSets.convert(new FilterSets(q)
								.updateFilterSet(FilterSets.convertDO(request
										.getSet()), s.nextRevision()), s
								.getUid()));
						return response;
					}
				});
	}

}
