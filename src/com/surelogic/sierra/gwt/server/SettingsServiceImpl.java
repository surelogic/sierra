package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FilterSet;
import com.surelogic.sierra.gwt.client.service.SettingsService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.settings.FilterEntryDO;
import com.surelogic.sierra.jdbc.settings.FilterSetDO;
import com.surelogic.sierra.jdbc.settings.ParentDO;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.FindingType;

public class SettingsServiceImpl extends SierraServiceServlet implements
		SettingsService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6781260512153199775L;

	public List<FilterSet> getFilterSets() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<FilterSet>>() {

					public List<FilterSet> perform(Connection conn,
							Server server, User user) throws Exception {
						final Map<String, FilterSet> sets = new HashMap<String, FilterSet>();
						for (FilterSetDO detail : SettingsManager
								.getInstance(conn).listFilterSets()) {
							final FilterSet set = getOrCreateSet(detail
									.getUid(), sets);
							set.setName(detail.getName());
							final List<FilterSet> parents = new ArrayList<FilterSet>();
							for (ParentDO parent : detail.getParents()) {
								parents.add(getOrCreateSet(parent.getUid(),
										sets));
							}
							set.setParents(parents);
							final List<FilterEntry> filters = new ArrayList<FilterEntry>();
							for (FilterEntryDO fDetail : detail
									.getFilters()) {
								final FilterEntry filter = new FilterEntry();
								filter.setFiltered(fDetail.isFiltered());
								final FindingType type = fDetail
										.getFindingType();
								filter.setName(type.getName());
								filter.setId(type.getId());
								filter.setShortMessage(type.getShortMessage());
								filters.add(filter);
							}
							set.setEntries(filters);
						}
						return new ArrayList<FilterSet>(sets.values());
					}
				});
	}

	private static FilterSet getOrCreateSet(String uid,
			Map<String, FilterSet> sets) {
		FilterSet set = sets.get(uid);
		if (set == null) {
			set = new FilterSet();
			set.setUuid(uid);
			sets.put(uid, set);
		}
		return set;
	}
}
