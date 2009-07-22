package com.surelogic.sierra.tool.message;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.jdbc.tool.ExtensionDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;

public class BugLinkServiceImpl extends SecureServiceServlet implements
		BugLinkService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8719740627758175475L;

	public CreateCategoryResponse createCategory(
			final CreateCategoryRequest request) {
		return ConnectionFactory.getInstance().withTransaction(
				new ServerQuery<CreateCategoryResponse>() {
					public CreateCategoryResponse perform(final Query q,
							final Server s) {
						final Categories sets = new Categories(q);
						final long revision = s.nextRevision();
						final CategoryDO set = sets.createCategory(request
								.getName(), request.getDescription(), revision);
						final Set<CategoryEntryDO> entries = set.getFilters();
						for (final FilterEntry e : request.getFilter()) {
							entries.add(new CategoryEntryDO(e.getType(), e
									.isFiltered()));
						}
						final Set<String> parents = set.getParents();
						for (final String p : request.getParent()) {
							parents.add(p);
						}
						q.prepared("Definitions.insertDefinition").call(
								set.getUid(), s.getUid());
						final CreateCategoryResponse response = new CreateCategoryResponse();
						response.setSet(Categories.convert(sets.updateCategory(
								set, revision), s.getUid()));
						return response;
					}
				});
	}

	public ListCategoryResponse listCategories(final ListCategoryRequest request) {
		return ConnectionFactory.getInstance().withReadOnly(
				new ServerQuery<ListCategoryResponse>() {

					public ListCategoryResponse perform(final Query q,
							final Server s) {
						final ListCategoryResponse response = new ListCategoryResponse();
						// Filter out all servers w/ that do not have a newer
						// revision than the client claims to have.
						final HashMap<String, Long> servers = new HashMap<String, Long>();
						q.statement("FilterSets.latestServerRevisions",
								new RowHandler<Void>() {
									public Void handle(final Row r) {
										servers.put(r.nextString(), r
												.nextLong());
										return null;
									}
								}).call();
						final Queryable<List<String>> deletions = q.prepared(
								"FilterSets.listServerDeletions",
								new StringRowHandler());
						for (final ServerRevision r : request
								.getServerRevisions()) {
							final Long revision = servers.get(r.getServer());
							if (revision != null) {
								// Don't return any categories from this server
								// if nothing new
								// has changed
								if (r.getRevision() >= revision) {
									servers.remove(r.getServer());
								}
								// Find any deletions to report
								response.getDeletions().addAll(
										deletions.call(r.getServer(), r
												.getRevision()));
							}
						}
						final Categories cats = new Categories(q);
						final List<FilterSet> sets = response.getFilterSets();
						for (final String server : servers.keySet()) {
							for (final CategoryDO set : cats
									.listServerCategories(server)) {
								// TODO we probably want to only return newly
								// updated categories
								sets.add(Categories.convert(set, server));
							}
						}
						final Set<ExtensionName> dependencySet = new HashSet<ExtensionName>();
						for (final FilterSet f : response.getFilterSets()) {
							dependencySet.addAll(cats.calculateDependencies(f
									.getUid()));
						}
						response.getDependencies().addAll(dependencySet);
						return response;
					}
				});
	}

	public UpdateCategoryResponse updateCategory(
			final UpdateCategoryRequest request) throws RevisionException {
		return ConnectionFactory.getInstance().withTransaction(
				new ServerQuery<UpdateCategoryResponse>() {
					public UpdateCategoryResponse perform(final Query q,
							final Server s) {
						final UpdateCategoryResponse response = new UpdateCategoryResponse();
						response.setSet(Categories.convert(new Categories(q)
								.updateCategory(Categories.convertDO(request
										.getSet()), s.nextRevision()), s
								.getUid()));
						return response;
					}
				});
	}

	public CreateScanFilterResponse createScanFilter(
			final CreateScanFilterRequest request) {
		return ConnectionFactory.getInstance().withTransaction(
				new ServerQuery<CreateScanFilterResponse>() {
					public CreateScanFilterResponse perform(final Query q,
							final Server s) {
						final ScanFilters filters = new ScanFilters(q);
						final long revision = s.nextRevision();
						final ScanFilterDO filter = filters.createScanFilter(
								request.getName(), revision);
						final Set<CategoryFilterDO> cSet = filter
								.getCategories();
						for (final CategoryFilter c : request
								.getCategoryFilter()) {
							cSet.add(new CategoryFilterDO(c.getUid(), c
									.getImportance()));
						}
						final Set<TypeFilterDO> tSet = filter.getFilterTypes();
						for (final TypeFilter t : request.getTypeFilter()) {
							tSet.add(new TypeFilterDO(t.getUid(), t
									.getImportance(), t.isFiltered()));
						}
						filters.updateScanFilter(filter, revision);
						q.prepared("Definitions.insertDefinition").call(
								filter.getUid(), s.getUid());
						final CreateScanFilterResponse response = new CreateScanFilterResponse();
						response.setFilter(ScanFilters.convert(filter, s
								.getUid()));
						return response;
					}
				});

	}

	public ListScanFilterResponse listScanFilters(
			final ListScanFilterRequest request) {
		return ConnectionFactory.getInstance().withReadOnly(
				new ServerQuery<ListScanFilterResponse>() {
					public ListScanFilterResponse perform(final Query q,
							final Server s) {
						final ListScanFilterResponse response = new ListScanFilterResponse();
						// Filter out all servers w/ that do not have a newer
						// revision than the client claims to have.
						final HashMap<String, Long> servers = new HashMap<String, Long>();
						q.statement("ScanFilters.latestServerRevisions",
								new RowHandler<Void>() {
									public Void handle(final Row r) {
										servers.put(r.nextString(), r
												.nextLong());
										return null;
									}
								}).call();
						final Queryable<List<String>> deletions = q.prepared(
								"ScanFilters.listServerDeletions",
								new StringRowHandler());
						for (final ServerRevision r : request
								.getServerRevisions()) {
							final Long revision = servers.get(r.getServer());
							if (revision != null) {
								// Don't return any categories from this server
								// if nothing new
								// has changed
								if (r.getRevision() >= revision) {
									servers.remove(r.getServer());
								}
								// Find any deletions to report
								response.getDeletions().addAll(
										deletions.call(r.getServer(), r
												.getRevision()));
							}
						}
						final List<ScanFilter> list = response.getScanFilter();
						final ScanFilters filters = new ScanFilters(q);
						for (final String server : servers.keySet()) {
							for (final ScanFilterDO set : filters
									.listServerScanFilters(server)) {
								// TODO we probably want to only return newly
								// updated categories
								list.add(ScanFilters.convert(set, server));
							}
						}
						final Set<ExtensionName> dependencySet = new HashSet<ExtensionName>();
						for (final ScanFilter f : response.getScanFilter()) {
							dependencySet.addAll(filters
									.calculateDependencies(f.getUid()));
						}
						response.getDependencies().addAll(dependencySet);
						return response;
					}
				});
	}

	public UpdateScanFilterResponse updateScanFilter(
			final UpdateScanFilterRequest request) throws RevisionException {
		return ConnectionFactory.getInstance().withTransaction(
				new ServerQuery<UpdateScanFilterResponse>() {
					public UpdateScanFilterResponse perform(final Query q,
							final Server s) {
						final UpdateScanFilterResponse response = new UpdateScanFilterResponse();
						final ScanFilterDO sf = ScanFilters.convertDO(request
								.getFilter());
						final ScanFilter updated = ScanFilters.convert(
								new ScanFilters(q).updateScanFilter(sf, s
										.nextRevision()), s.getUid());
						response.setFilter(updated);
						return response;
					}
				});
	}

	public ListExtensionResponse listExtensions(
			final ListExtensionRequest request) {
		return ConnectionFactory.getInstance().withReadOnly(
				new DBQuery<ListExtensionResponse>() {
					public ListExtensionResponse perform(final Query q) {
						final ListExtensionResponse r = new ListExtensionResponse();
						for (final ExtensionDO d : new FindingTypes(q)
								.getExtensions()) {
							r.getExtensions().add(FindingTypes.convert(d));
						}
						return r;
					}
				});
	}

	public RegisterExtensionResponse registerExtension(
			final RegisterExtensionRequest request) {
		final ExtensionDO eDO = FindingTypes.convertDO(request.getExtension());
		return ConnectionFactory.getInstance().withTransaction(
				new DBQuery<RegisterExtensionResponse>() {
					public RegisterExtensionResponse perform(final Query q) {
						new FindingTypes(q).registerExtension(eDO);
						return new RegisterExtensionResponse();
					}
				});
	}

	public File downloadExtension(final ExtensionName request) {
		// TODO
		return null;
	}

	public GetExtensionsResponse getExtensions(
			final GetExtensionsRequest request) {
		return ConnectionFactory.getInstance().withReadOnly(
				new DBQuery<GetExtensionsResponse>() {
					public GetExtensionsResponse perform(final Query q) {
						final GetExtensionsResponse response = new GetExtensionsResponse();
						for (final ExtensionName e : request.getExtensions()) {
							final ExtensionDO eDO = new FindingTypes(q)
									.getExtension(e.getName(), e.getVersion());
							if (eDO != null) {
								response.getExtension().add(
										FindingTypes.convert(eDO));
							}
						}
						return response;
					}
				});
	}

	public EnsureExtensionResponse ensureExtensions(
			final EnsureExtensionRequest request) {
		final EnsureExtensionResponse response = new EnsureExtensionResponse();
		response.getUnknownExtensions().addAll(request.getExtensions());
		response.getUnknownExtensions().removeAll(
				ConnectionFactory.getInstance().withReadOnly(
						SettingQueries.localExtensions()));
		return response;
	}
}
