package com.surelogic.sierra.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.ResultHandler;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringResultHandler;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Category;
import com.surelogic.sierra.gwt.client.data.FindingType;
import com.surelogic.sierra.gwt.client.data.FindingTypeFilter;
import com.surelogic.sierra.gwt.client.data.ImportanceView;
import com.surelogic.sierra.gwt.client.data.Project;
import com.surelogic.sierra.gwt.client.data.ReportSettings;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ScanFilter;
import com.surelogic.sierra.gwt.client.data.ScanFilterEntry;
import com.surelogic.sierra.gwt.client.data.ServerLocation;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.FindingType.ArtifactTypeInfo;
import com.surelogic.sierra.gwt.client.data.Report.OutputType;
import com.surelogic.sierra.gwt.client.data.ServerLocation.Protocol;
import com.surelogic.sierra.gwt.client.data.cache.ReportCache;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings;
import com.surelogic.sierra.gwt.client.data.dashboard.ReportWidget;
import com.surelogic.sierra.gwt.client.data.dashboard.DashboardSettings.DashboardRow;
import com.surelogic.sierra.gwt.client.service.SettingsService;
import com.surelogic.sierra.jdbc.RevisionException;
import com.surelogic.sierra.jdbc.dashboard.DashboardQueries;
import com.surelogic.sierra.jdbc.project.ProjectDO;
import com.surelogic.sierra.jdbc.project.Projects;
import com.surelogic.sierra.jdbc.reports.ReportSettingQueries;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.server.UserQuery;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.ServerLocations;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.SierraServerLocation;

public class SettingsServiceImpl extends SierraServiceServlet implements
		SettingsService {
	private static final long serialVersionUID = 6781260512153199775L;

	public List<String> searchProjects(final String query, final int limit) {
		return ConnectionFactory.getInstance().withUserReadOnly(
				new UserQuery<List<String>>() {
					public List<String> perform(final Query q, final Server s,
							final User u) {
						return q.prepared("Projects.query",
								new ResultHandler<List<String>>() {
									public List<String> handle(
											final com.surelogic.common.jdbc.Result r) {
										final List<String> result = new ArrayList<String>();
										final int count = 0;
										for (final Row row : r) {
											if ((limit == -1)
													|| (count < limit)) {
												result.add(row.nextString());
											} else {
												return result;
											}
										}
										return result;
									}

								}).call(query.replace('*', '%').concat("%"));
					}
				});
	}

	public List<Project> getProjects() {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<List<Project>>() {
					public List<Project> perform(final Query q, final Server s,
							final User u) {
						final List<Project> result = new ArrayList<Project>();
						final Projects projects = new Projects(q);
						final ScanFilters filters = new ScanFilters(q);
						final FindingTypes types = new FindingTypes(q);
						final Categories cats = new Categories(q);
						for (final ProjectDO projectDO : projects
								.listProjects()) {
							final Project prj = new Project();
							prj.setUuid(projectDO.getUuid());
							prj.setName(projectDO.getName());
							final String sfUuid = projectDO.getScanFilter();
							prj.setScanFilter(getFilter(filters
									.getScanFilter(sfUuid), types, cats));
							result.add(prj);
						}
						return result;
					}
				});
	}

	public List<Category> getCategories() {
		return ConnectionFactory.getInstance().withUserReadOnly(
				new UserQuery<List<Category>>() {

					public List<Category> perform(final Query q,
							final Server server, final User user) {
						final String serverUID = server.getUid();
						final Map<String, Category> sets = new HashMap<String, Category>();
						final FindingTypes types = new FindingTypes(q);
						final Categories fs = new Categories(q);
						/*
						 * Get the server locations that we know about, keyed by
						 * label
						 */
						final Map<String, SierraServerLocation> locations = new HashMap<String, SierraServerLocation>();
						for (final SierraServerLocation location : ServerLocations
								.fetchQuery(null).perform(q).keySet()) {
							locations.put(location.getLabel(), location);
						}
						/*
						 * Retrieves the server uuid and label for the category
						 */
						final Queryable<String[]> categoryServer = q.prepared(
								"Definitions.getDefinitionServer",
								SingleRowHandler
										.from(new RowHandler<String[]>() {
											public String[] handle(
													final com.surelogic.common.jdbc.Row row) {
												return new String[] {
														row.nextString(),
														row.nextString() };
											}
										}));
						for (final CategoryDO detail : fs.listCategories()) {
							final Category set = getOrCreateSet(
									detail.getUid(), sets);
							set.setName(detail.getName());
							final String[] owningServer = categoryServer
									.call(detail.getUid());
							set.setLocal((owningServer != null)
									&& serverUID.equals(owningServer[0]));
							if (owningServer != null) {
								set.setOwnerLabel(owningServer[1]);
								final SierraServerLocation owner = locations
										.get(owningServer[1]);
								if (owner != null) {
									final StringBuilder urlBuf = new StringBuilder(
											owner.createHomeURL().toString());
									urlBuf.append("#categories/uuid=");
									urlBuf.append(detail.getUid());
									set.setOwnerURL(urlBuf.toString());
								}
							}
							String info = StringUtils.remove(detail.getInfo(),
									'\t');
							info = StringUtils.replaceChars(info, '\n', ' ');
							set.setInfo(info);
							final Set<Category> parents = new HashSet<Category>();
							for (final String parent : detail.getParents()) {
								parents.add(getOrCreateSet(parent, sets));
							}
							set.setParents(parents);
							final Set<FindingTypeFilter> filters = new HashSet<FindingTypeFilter>();
							for (final CategoryEntryDO fDetail : detail
									.getFilters()) {
								final FindingTypeFilter filter = new FindingTypeFilter();
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
							q.prepared("FilterSets.scanFiltersUsing",
									new RowHandler<Void>() {
										public Void handle(final Row r) {
											set
													.getScanFiltersUsing()
													.add(
															new Category.ScanFilterInfo(
																	r
																			.nextString(),
																	r
																			.nextString()));
											return null;
										}
									}).call(detail.getId());
						}
						final List<Category> values = new ArrayList<Category>(
								sets.values());
						Collections.sort(values);
						return values;
					}
				});
	}

	private static Category getOrCreateSet(final String uid,
			final Map<String, Category> sets) {
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
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Result<String>>() {
					public Result<String> perform(final Query q,
							final Server s, final User u) {
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
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Status>() {
					public Status perform(final Query q, final Server s,
							final User u) {
						if (!s.getUid().equals(
								q.prepared("Definitions.getDefinitionServer",
										new StringResultHandler()).call(
										c.getUuid()))) {
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
						final Set<FindingTypeFilter> entries = c.getEntries();
						for (final FindingTypeFilter entry : entries) {
							entrySet.add(new CategoryEntryDO(entry.getUuid(),
									entry.isFiltered()));
						}
						try {
							sets.updateCategory(set, revision);
						} catch (final RevisionException e) {
							return Status.failure(e.getMessage());
						} catch (final IllegalArgumentException e) {
							return Status.failure(e.getMessage());
						}
						return Status.success("Category " + c.getName()
								+ " updated.");
					}
				});
	}

	public Status deleteCategory(final String uuid) {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Status>() {
					public Status perform(final Query q, final Server s,
							final User u) {
						final Categories cats = new Categories(q);
						final String server = s.getUid();
						if (server.equals(q.prepared(
								"Definitions.getDefinitionServer",
								new StringResultHandler()).call(uuid))) {
							try {
								q.prepared("FilterSets.insertDeletedFilterSet")
										.call(uuid, server, s.nextRevision());
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

	public List<FindingType> getFindingTypes() {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<List<FindingType>>() {
					public List<FindingType> perform(final Query q,
							final Server s, final User u) {
						final List<FindingType> result = new ArrayList<FindingType>();
						final FindingTypes types = new FindingTypes(q);
						for (final FindingTypeDO type : types
								.listFindingTypes()) {
							result.add(getType(type, q));
						}
						return result;
					}
				});
	}

	public Result<FindingType> getFindingType(final String uuid) {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Result<FindingType>>() {
					public Result<FindingType> perform(final Query q,
							final Server s, final User u) {
						final FindingTypes types = new FindingTypes(q);
						final FindingTypeDO type = types.getFindingType(uuid);
						if (type != null) {
							return Result.success("Finding Type Found",
									getType(type, q));
						} else {
							return Result
									.failure("No finding type found", null);
						}
					}
				});
	}

	private FindingType getType(final FindingTypeDO type, final Query q) {
		final FindingType info = new FindingType();
		info.setInfo(type.getInfo());
		info.setName(type.getName());
		info.setShortMessage(type.getShortMessage());
		info.setUuid(type.getUid());
		final Map<ArtifactTypeInfo, ArtifactTypeInfo> set = new HashMap<ArtifactTypeInfo, ArtifactTypeInfo>();
		for (final ArtifactTypeDO artDO : type.getArtifactTypes()) {
			final ArtifactTypeInfo newInfo = new ArtifactTypeInfo(artDO
					.getTool(), artDO.getMnemonic());
			final ArtifactTypeInfo oldInfo = set.get(newInfo);
			if (oldInfo == null) {
				set.put(newInfo, newInfo);
				newInfo.getVersions().add(artDO.getVersion());
			} else {
				oldInfo.getVersions().add(artDO.getVersion());
			}
		}
		info.getArtifactTypes().addAll(set.keySet());
		q.prepared("FindingTypes.categoriesReferencing",
				new RowHandler<Void>() {
					public Void handle(final Row r) {
						final FindingType.CategoryInfo ci = new FindingType.CategoryInfo(
								r.nextString(), r.nextString(), r.nextString());
						if (r.nextBoolean()) {
							info.getCategoriesExcluding().add(ci);
						} else {
							info.getCategoriesIncluding().add(ci);
						}
						return null;
					}
				}).call(type.getId());
		q.prepared("FindingTypes.scanFiltersIncluding", new RowHandler<Void>() {
			public Void handle(final Row r) {
				info.getScanFiltersIncluding().add(
						new FindingType.ScanFilterInfo(r.nextString(), r
								.nextString()));
				return null;
			}
		}).call(type.getId());
		return info;
	}

	public List<ScanFilter> getScanFilters() {
		return ConnectionFactory.getInstance().withReadOnly(
				new ServerQuery<List<ScanFilter>>() {

					public List<ScanFilter> perform(final Query q,
							final Server s) {
						final String serverUID = s.getUid();
						final Queryable<String> definitionServer = q.prepared(
								"Definitions.getDefinitionServer",
								new StringResultHandler());

						final FindingTypes ft = new FindingTypes(q);
						final Categories fs = new Categories(q);
						final List<ScanFilter> list = new ArrayList<ScanFilter>();
						for (final ScanFilterDO fDO : new ScanFilters(q)
								.listScanFilters()) {
							final ScanFilter filter = getFilter(fDO, ft, fs);
							filter.setLocal(serverUID.equals(definitionServer
									.call(filter.getUuid())));
							list.add(filter);
						}
						return list;
					}
				});
	}

	protected ScanFilter getFilter(final ScanFilterDO fDO,
			final FindingTypes ft, final Categories cs) {
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
			e.setUuid(c.getUid());
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
			e.setUuid(tDO.getUid());
			filters.add(e);
		}
		return f;
	}

	public ScanFilter createScanFilter(final String name) {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<ScanFilter>() {
					public ScanFilter perform(final Query q, final Server s,
							final User u) {
						final ScanFilters filters = new ScanFilters(q);
						final ScanFilter f = getFilter(filters
								.createScanFilter(name, s.nextRevision()),
								new FindingTypes(q), new Categories(q));
						f.setLocal(true);
						q.prepared("Definitions.insertDefinition").call(
								f.getUuid(), s.getUid());
						return f;
					}
				});
	}

	public Status updateScanFilter(final ScanFilter f) {
		final ScanFilterDO fDO = new ScanFilterDO();
		fDO.setName(f.getName());
		fDO.setRevision(f.getRevision());
		fDO.setUid(f.getUuid());
		final Set<CategoryFilterDO> cats = fDO.getCategories();
		final Set<TypeFilterDO> types = fDO.getFilterTypes();
		for (final ScanFilterEntry e : f.getCategories()) {
			final CategoryFilterDO c = new CategoryFilterDO();
			c.setImportance(importance(e.getImportance()));
			c.setUid(e.getUuid());
			cats.add(c);
		}
		for (final ScanFilterEntry e : f.getTypes()) {
			final TypeFilterDO t = new TypeFilterDO();
			t.setImportance(importance(e.getImportance()));
			t.setFiltered(false);
			t.setFindingType(e.getUuid());
			types.add(t);
		}
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Status>() {
					public Status perform(final Query q, final Server s,
							final User u) {
						if (!s.getUid().equals(
								q.prepared("Definitions.getDefinitionServer",
										new StringResultHandler()).call(
										fDO.getUid()))) {
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
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Status>() {
					public Status perform(final Query q, final Server s,
							final User u) {
						return ConnectionFactory.getInstance()
								.withUserTransaction(new UserQuery<Status>() {
									public Status perform(final Query q,
											final Server s, final User u) {
										final ScanFilters filters = new ScanFilters(
												q);
										final String server = s.getUid();
										if (server
												.equals(q
														.prepared(
																"Definitions.getDefinitionServer",
																new StringResultHandler())
														.call(uuid))) {
											try {
												q
														.prepared(
																"ScanFilters.insertDeletedScanFilter")
														.call(
																uuid,
																server,
																s
																		.nextRevision());
												filters.deleteScanFilter(uuid);
											} catch (final IllegalArgumentException e) {
												return Status.failure(e
														.getMessage());
											}
										} else {
											return Status
													.failure("The scan filter does not belong to this server and cannot be deleted.");
										}
										return Status
												.success("Category deleted");
									}
								});
					}
				});
	}

	private static Importance importance(final ImportanceView i) {
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

	private static ImportanceView view(final Importance i) {
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

	@SuppressWarnings("unchecked")
	public List<ServerLocation> listServerLocations() {
		final List<ServerLocation> servers = new ArrayList<com.surelogic.sierra.gwt.client.data.ServerLocation>();
		for (final SierraServerLocation l : ConnectionFactory
				.getInstance()
				.withReadOnly(ServerLocations.fetchQuery(Collections.EMPTY_MAP))
				.keySet()) {
			final ServerLocation s = new ServerLocation();
			s.setContext(l.getContextPath());
			s.setHost(l.getHost());
			s.setLabel(l.getLabel());
			s.setPass(l.getPass());
			s.setPort(l.getPort());
			s.setProtocol(ServerLocation.Protocol.fromValue(l.getProtocol()));
			s.setUser(l.getUser());
			servers.add(s);
		}
		return servers;
	}

	public Status deleteServerLocation(final String label) {
		return ConnectionFactory.getInstance().withTransaction(
				new DBQuery<Status>() {

					public Status perform(final Query q) {
						final Map<SierraServerLocation, Collection<String>> servers = ServerLocations
								.fetchQuery(null).perform(q);
						SierraServerLocation loc = null;
						for (final SierraServerLocation l : servers.keySet()) {
							if (l.getLabel().equals(label)) {
								loc = l;
								break;
							}
						}
						if (loc != null) {
							servers.remove(loc);
						}
						ServerLocations.saveQuery(servers).doPerform(q);
						return Status.success(label + " deleted.");
					}
				});
	}

	public Status saveServerLocation(final ServerLocation loc) {
		return ConnectionFactory.getInstance().withTransaction(
				new DBQuery<Status>() {

					public Status perform(final Query q) {
						final Map<SierraServerLocation, Collection<String>> servers = ServerLocations
								.fetchQuery(null).perform(q);
						final SierraServerLocation l = new SierraServerLocation(
								loc.getLabel(), loc.getHost(), loc
										.getProtocol() == Protocol.HTTPS, loc
										.getPort(), loc.getContext(), loc
										.getUser(), loc.getPass());
						Collection<String> projects = servers.get(l);
						if (projects == null) {
							projects = Collections.emptyList();
						}
						servers.remove(l);
						servers.put(l, projects);
						ServerLocations.saveQuery(servers).doPerform(q);
						return Status.success(loc.getLabel() + " updated.");
					}
				});

	}

	public Status saveProjectFilter(final String project,
			final String scanFilter) {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserQuery<Status>() {
					public Status perform(final Query query,
							final Server server, final User user) {
						new Projects(query).updateProjectFilter(project,
								scanFilter);
						return Status.success();
					}
				});

	}

	public List<ReportSettings> listReportSettings() {
		return ConnectionFactory.getInstance().withUserReadOnly(
				ReportSettingQueries.listUserQueries());
	}

	public Status saveReportSettings(final ReportSettings settings) {
		if (settings.getUuid() == null) {
			settings.setUuid(UUID.randomUUID().toString());
		}
		ConnectionFactory.getInstance().withUserTransaction(
				ReportSettingQueries.save(settings));
		return Status.success("Settings saved.");
	}

	public Status deleteReportSettings(final String uuid) {
		ConnectionFactory.getInstance().withUserTransaction(
				ReportSettingQueries.delete(uuid));
		return Status.success("Settings deleted");
	}

	public DashboardSettings getDashboardSettings() {

		DashboardSettings settings = ConnectionFactory.getInstance()
				.withUserReadOnly(DashboardQueries.getDashboard());
		if (settings == null) {
			settings = new DashboardSettings();

			final DashboardRow row0 = settings.getRow(0, true);

			final ReportSettings latestScans = new ReportSettings(ReportCache
					.latestScans());
			latestScans.setWidth("450");
			row0.setLeftColumn(new ReportWidget(latestScans, OutputType.CHART));

			final ReportSettings auditContribs = new ReportSettings(ReportCache
					.userAudits());
			latestScans.setWidth("320");
			row0.setRightColumn(new ReportWidget(auditContribs,
					OutputType.CHART));

			final DashboardRow row1 = settings.getRow(1, true);
			final ReportSettings userAudits = new ReportSettings(ReportCache
					.userAudits());
			row1.setRightColumn(new ReportWidget(userAudits, OutputType.TABLE));

			final DashboardRow row2 = settings.getRow(2, true);
			final ReportSettings publishedProjects = new ReportSettings(
					ReportCache.publishedProjects());
			row2.setSingleColumn(new ReportWidget(publishedProjects,
					OutputType.TABLE));
		}
		return settings;
	}

	public Status saveDashboardSettings(final DashboardSettings settings) {
		ConnectionFactory.getInstance().withUserTransaction(
				DashboardQueries.updateDashboard(settings));
		return Status.success();
	}
}