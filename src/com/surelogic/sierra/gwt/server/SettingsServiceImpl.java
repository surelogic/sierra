package com.surelogic.sierra.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FilterEntry;
import com.surelogic.sierra.gwt.client.data.FindingTypeInfo;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.service.SettingsService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Importance;

public class SettingsServiceImpl extends SierraServiceServlet implements
		SettingsService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6781260512153199775L;

	public Map<String, String> searchCategories(final String query,
			final int limit) {
		return ConnectionFactory
				.withUserReadOnly(new UserQuery<Map<String, String>>() {
					public Map<String, String> perform(Query q, Server s, User u) {
						return q.prepared("FilterSets.query",
								new QueryHandler(limit)).call(
								query.replace("*", "%").concat("%"));
					}
				});
	}

	public Map<String, String> searchFindingTypes(final String query,
			final int limit) {
		return ConnectionFactory
				.withUserReadOnly(new UserQuery<Map<String, String>>() {
					public Map<String, String> perform(Query q, Server s, User u) {
						return q.prepared("FindingTypes.query",
								new QueryHandler(limit)).call(
								query.replace('*', '%').concat("%"));
					}
				});
	}

	/**
	 * Handles a result set with two columns. The first is the key, and the
	 * second is the value.
	 * 
	 * @author nathan
	 * 
	 */
	private static class QueryHandler implements
			ResultHandler<Map<String, String>> {
		private final int limit;

		QueryHandler(int limit) {
			this.limit = limit;
		}

		public Map<String, String> handle(com.surelogic.common.jdbc.Result r) {
			final Map<String, String> results = new HashMap<String, String>();
			int count = 0;
			for (final Row row : r) {
				if (count++ < limit) {
					results.put(row.nextString(), row.nextString());
				} else {
					return results;
				}
			}
			return results;
		}
	}

	public List<Category> getCategories() {
		return ConnectionFactory
				.withUserReadOnly(new UserQuery<List<Category>>() {

					public List<Category> perform(Query q, Server server,
							User user) {
						final Map<String, Category> sets = new HashMap<String, Category>();
						final FindingTypes types = new FindingTypes(q);
						final Categories fs = new Categories(q);
						for (final CategoryDO detail : fs.listCategories()) {
							final Category set = getOrCreateSet(
									detail.getUid(), sets);
							set.setName(detail.getName());
							String info = StringUtils.remove(detail.getInfo(),
									'\t');
							info = StringUtils.replaceChars(info, '\n', ' ');
							set.setInfo(info);
							final Set<Category> parents = new HashSet<Category>();
							for (final String parent : detail.getParents()) {
								parents.add(getOrCreateSet(parent, sets));
							}
							set.setParents(parents);
							final Set<FilterEntry> filters = new HashSet<FilterEntry>();
							for (final CategoryEntryDO fDetail : detail
									.getFilters()) {
								final FilterEntry filter = new FilterEntry();
								filter.setFiltered(fDetail.isFiltered());
								final FindingTypeDO type = types
										.getFindingType(fDetail
												.getFindingType());
								filter.setName(type.getName());
								filter.setUuid(type.getUid());
								filter.setShortMessage(type.getShortMessage());
								filters.add(filter);
							}
							set.setEntries(filters);
							set.setRevision(detail.getRevision());
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

	public Result<String> createCategory(final String name,
			final List<String> entries, final List<String> parents) {
		return ConnectionFactory
				.withUserTransaction(new UserQuery<Result<String>>() {
					public Result<String> perform(Query q, Server s, User u) {
						final Categories sets = new Categories(q);
						final long revision = s.nextRevision();
						final CategoryDO set = sets.createCategory(name, null,
								revision);
						set.getParents().addAll(parents);
						final Set<CategoryEntryDO> doEntries = set.getFilters();
						final List<String> moEntries = entries;
						for (final String entry : moEntries) {
							doEntries.add(new CategoryEntryDO(entry, true));
						}
						sets.updateCategory(set, revision);
						q.prepared("Definitions.insertDefinition").call(
								set.getUid(), s.getUid());
						return Result.success("Filter set " + name
								+ " created.", set.getUid());
					}
				});

	}

	// TODO do this w/o passing back and forth the whole graph
	public Status updateCategory(final Category c) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			public Status perform(Query q, Server s, User u) {
				if (!s.getUid().equals(
						q.prepared("Definitions.getDefinitionServer",
								new StringResultHandler()).call(c.getUuid()))) {
					return Status
							.failure("This category is not owned by this server, and cannot be updated.");
				}
				final Categories sets = new Categories(q);
				final long revision = s.nextRevision();
				final CategoryDO set = new CategoryDO();
				set.setUid(c.getUuid());
				set.setInfo(c.getInfo());
				set.setName(c.getName());
				set.setRevision(c.getRevision());
				final Set<String> parentSet = set.getParents();
				final Set<Category> parents = c.getParents();
				for (final Category parent : parents) {
					parentSet.add(parent.getUuid());
				}
				final Set<CategoryEntryDO> entrySet = set.getFilters();
				final Set<FilterEntry> entries = c.getEntries();
				for (final FilterEntry entry : entries) {
					entrySet.add(new CategoryEntryDO(entry.getUuid(), entry
							.isFiltered()));
				}
				try {
					sets.updateCategory(set, revision);
				} catch (final RevisionException e) {
					return Status.failure(e.getMessage());
				} catch (final IllegalArgumentException e) {
					return Status.failure(e.getMessage());
				}
				return Status.success("Category " + c.getName() + " updated.");
			}
		});
	}

	public Status deleteCategory(final String uuid) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			public Status perform(Query q, Server s, User u) {
				final Categories cats = new Categories(q);
				final String server = s.getUid();
				if (server.equals(q.prepared("Definitions.getDefinitionServer",
						new StringResultHandler()).call(uuid))) {
					try {
						q.prepared("FilterSets.insertDeletedFilterSet").call(
								uuid, server, s.nextRevision());
						cats.deleteCategory(uuid);
					} catch (final IllegalArgumentException e) {
						return Status.failure(e.getMessage());
					}
				} else {
					return Status
							.failure("The category does not belong to this server and cannot be deleted.");
				}
				return Status.success("Category deleted");
			}
		});
	}

	public Result<FindingTypeInfo> getFindingTypeInfo(final String uid) {
		return ConnectionFactory
				.withUserTransaction(new UserQuery<Result<FindingTypeInfo>>() {
					public Result<FindingTypeInfo> perform(Query q, Server s,
							User u) {
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
							return Result
									.failure("No finding type found", null);
						}
					}
				});

	}

	public List<ScanFilter> getScanFilters() {
		return ConnectionFactory
				.withReadOnly(new ServerQuery<List<ScanFilter>>() {

					public List<ScanFilter> perform(Query q, Server s) {
						final FindingTypes ft = new FindingTypes(q);
						final Categories fs = new Categories(q);
						final List<ScanFilter> list = new ArrayList<ScanFilter>();
						for (final ScanFilterDO fDO : new ScanFilters(q)
								.listScanFilters()) {
							list.add(getFilter(fDO, ft, fs));
						}
						return list;
					}
				});
	}

	protected ScanFilter getFilter(ScanFilterDO fDO, FindingTypes ft,
			Categories cs) {
		final ScanFilter f = new ScanFilter();
		f.setName(fDO.getName());
		f.setRevision(fDO.getRevision());
		f.setUuid(fDO.getUid());
		Set<ScanFilterEntry> filters = f.getCategories();
		for (final CategoryFilterDO c : fDO.getCategories()) {
			final ScanFilterEntry e = new ScanFilterEntry();
			e.setCategory(true);
			e.setImportance(view(c.getImportance()));
			final CategoryDO catDO = cs.getCategory(c.getUid());
			e.setName(catDO.getName());
			e.setShortMessage(catDO.getInfo());
			e.setUid(c.getUid());
			filters.add(e);
		}
		filters = f.getTypes();
		for (final TypeFilterDO t : fDO.getFilterTypes()) {
			final ScanFilterEntry e = new ScanFilterEntry();
			e.setCategory(false);
			e.setImportance(view(t.getImportance()));
			final FindingTypeDO tDO = ft.getFindingType(t.getFindingType());
			e.setName(tDO.getName());
			e.setShortMessage(tDO.getShortMessage());
			e.setUid(tDO.getUid());
			filters.add(e);
		}
		return f;
	}

	public ScanFilter createScanFilter(final String name) {
		return ConnectionFactory
				.withUserTransaction(new UserQuery<ScanFilter>() {
					public ScanFilter perform(Query q, Server s, User u) {
						final ScanFilters filters = new ScanFilters(q);
						final ScanFilter f = getFilter(filters
								.createScanFilter(name, s.nextRevision()),
								new FindingTypes(q), new Categories(q));
						q.prepared("Definitions.insertDefinition").call(
								f.getUuid(), s.getUid());
						return f;
					}
				});
	}

	public Status updateScanFilter(ScanFilter f) {
		final ScanFilterDO fDO = new ScanFilterDO();
		fDO.setName(f.getName());
		fDO.setRevision(f.getRevision());
		fDO.setUid(f.getUuid());
		final Set<CategoryFilterDO> cats = fDO.getCategories();
		final Set<TypeFilterDO> types = fDO.getFilterTypes();
		for (final Object o : f.getCategories()) {
			final ScanFilterEntry e = (ScanFilterEntry) o;
			final CategoryFilterDO c = new CategoryFilterDO();
			c.setImportance(importance(e.getImportance()));
			c.setUid(e.getUid());
			cats.add(c);
		}
		for (final Object o : f.getTypes()) {
			final ScanFilterEntry e = (ScanFilterEntry) o;
			final TypeFilterDO t = new TypeFilterDO();
			t.setImportance(importance(e.getImportance()));
			t.setFiltered(false);
			t.setFindingType(e.getUid());
			types.add(t);
		}
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			public Status perform(Query q, Server s, User u) {
				if (!s.getUid().equals(
						q.prepared("Definitions.getDefinitionServer",
								new StringResultHandler()).call(fDO.getUid()))) {
					return Status
							.failure("This scan filter is not owned by this server, and cannot be updated.");
				}
				final ScanFilters sf = new ScanFilters(q);
				try {
					sf.updateScanFilter(fDO, s.nextRevision());
				} catch (final RevisionException e) {
					return Status
							.failure("Someone else has already updated this scan filter.");
				}
				return Status.success("Scan filter updated");
			}
		});

	}

	public Status deleteScanFilter(final String uuid) {
		return ConnectionFactory.withUserTransaction(new UserQuery<Status>() {
			public Status perform(Query q, Server s, User u) {
				return ConnectionFactory
						.withUserTransaction(new UserQuery<Status>() {
							public Status perform(Query q, Server s, User u) {
								final ScanFilters filters = new ScanFilters(q);
								final String server = s.getUid();
								if (server.equals(q.prepared(
										"Definitions.getDefinitionServer",
										new StringResultHandler()).call(uuid))) {
									try {
										q
												.prepared(
														"ScanFilters.insertDeletedScanFilter")
												.call(uuid, server,
														s.nextRevision());
										filters.deleteScanFilter(uuid);
									} catch (final IllegalArgumentException e) {
										return Status.failure(e.getMessage());
									}
								} else {
									return Status
											.failure("The scan filter does not belong to this server and cannot be deleted.");
								}
								return Status.success("Category deleted");
							}
						});
			}
		});
	}

	private static Importance importance(ImportanceView i) {
		if (i == null) {
			return null;
		}
		if (i == ImportanceView.CRITICAL) {
			return Importance.CRITICAL;
		} else if (i == ImportanceView.HIGH) {
			return Importance.HIGH;
		} else if (i == ImportanceView.IRRELEVANT) {
			return Importance.IRRELEVANT;
		} else if (i == ImportanceView.LOW) {
			return Importance.LOW;
		} else if (i == ImportanceView.MEDIUM) {
			return Importance.MEDIUM;
		}
		throw new IllegalStateException();
	}

	private static ImportanceView view(Importance i) {
		if (i == null) {
			return null;
		}
		switch (i) {
		case CRITICAL:
			return ImportanceView.CRITICAL;
		case HIGH:
			return ImportanceView.HIGH;
		case IRRELEVANT:
			return ImportanceView.IRRELEVANT;
		case LOW:
			return ImportanceView.LOW;
		case MEDIUM:
			return ImportanceView.MEDIUM;
		}
		throw new IllegalStateException();
	}

}