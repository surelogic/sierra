package com.surelogic.sierra.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.SettingsService;
import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.settings.FilterEntryDO;
import com.surelogic.sierra.jdbc.settings.FilterSetDO;
import com.surelogic.sierra.jdbc.settings.FilterSets;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.jdbc.user.User;

public class SettingsServiceImpl extends SierraServiceServlet implements
		SettingsService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6781260512153199775L;

	public List<Category> getCategories() {
		return ConnectionFactory
				.withUserReadOnly(new UserQuery<List<Category>>() {

					public List<Category> perform(Query q, Server server,
							User user) {
						final Map<String, Category> sets = new HashMap<String, Category>();
						final FindingTypes types = new FindingTypes(q);
						final FilterSets fs = new FilterSets(q);
						for (final FilterSetDO detail : fs.listFilterSets()) {
							final Category set = getOrCreateSet(
									detail.getUid(), sets);
							set.setName(detail.getName());
							set.setInfo(detail.getInfo());
							final Set<Category> parents = new HashSet<Category>();
							for (final String parent : detail.getParents()) {
								parents.add(getOrCreateSet(parent, sets));
							}
							set.setParents(parents);
							final Set<FilterEntry> filters = new HashSet<FilterEntry>();
							for (final FilterEntryDO fDetail : detail
									.getFilters()) {
								final FilterEntry filter = new FilterEntry();
								filter.setFiltered(fDetail.isFiltered());
								final FindingTypeDO type = types
										.getFindingType(fDetail
												.getFindingType());
								filter.setName(type.getName());
								filter.setUid(type.getUid());
								filter.setShortMessage(type.getShortMessage());
								filters.add(filter);
							}
							set.setEntries(filters);
						}
						final List<Category> values = new ArrayList<Category>(
								sets.values());
						Collections.sort(values, new Comparator<Category>() {
							public int compare(Category o1, Category o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						return values;
					}
				});
	}

	private static Category getOrCreateSet(String uid,
			Map<String, Category> sets) {
		Category set = sets.get(uid);
		if (set == null) {
			set = new Category();
			set.setUuid(uid);
			sets.put(uid, set);
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	public Status createCategory(final String name, final List entries,
			final List parents) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			public Status perform(Query q, Server s, User u) {
				final FilterSets sets = new FilterSets(q);
				final long revision = s.nextRevision();
				final FilterSetDO set = sets.createFilterSet(name, null,
						revision);
				set.getParents().addAll(parents);
				final Set<FilterEntryDO> doEntries = set.getFilters();
				final List<FilterEntry> moEntries = entries;
				for (final FilterEntry entry : moEntries) {
					doEntries.add(new FilterEntryDO(entry.getUid(), entry
							.isFiltered()));
				}
				sets.updateFilterSet(set, revision);
				return Status.success("Filter set " + name + " created.");
			}
		});

	}

	// TODO do this w/o passing back and forth the whole graph
	public Status updateCategory(final Category c) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			@SuppressWarnings("unchecked")
			public Status perform(Query q, Server s, User u) {
				final FilterSets sets = new FilterSets(q);
				final long revision = s.nextRevision();
				final FilterSetDO set = new FilterSetDO();
				set.setUid(c.getUuid());
				set.setInfo(c.getInfo());
				set.setName(c.getName());
				set.setRevision(c.getRevision());
				final Set<String> parentSet = set.getParents();
				final Set<Category> parents = c.getParents();
				for (final Category parent : parents) {
					parentSet.add(parent.getUuid());
				}
				final Set<FilterEntryDO> entrySet = set.getFilters();
				final Set<FilterEntry> entries = c.getEntries();
				for (final FilterEntry entry : entries) {
					entrySet.add(new FilterEntryDO(entry.getUid(), entry
							.isFiltered()));
				}
				try {
					sets.updateFilterSet(set, revision);
				} catch (final RevisionException e) {
					return Status.failure(e.getMessage());
				} catch (final IllegalArgumentException e) {
					return Status.failure(e.getMessage());
				}
				return Status.success("Category " + c.getName() + " updated.");
			}
		});
	}

	public Result getFindingTypeInfo(final String uid) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Result>() {
			public Result perform(Query q, Server s, User u) {
				final FindingTypes types = new FindingTypes(q);
				final FindingTypeDO type = types.getFindingType(uid);
				if (type != null) {
					final FindingTypeInfo info = new FindingTypeInfo();
					info.setInfo(type.getInfo());
					info.setName(type.getName());
					info.setShortMessage(type.getShortMessage());
					info.setUid(type.getUid());
					return Result.success("Finding Type Found", info);
				} else {
					return Result.failure("No finding type found");
				}
			}
		});

	}
}