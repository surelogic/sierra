package com.surelogic.sierra.tool.message;

import java.util.List;
import java.util.Set;

import com.surelogic.common.jdbc.Query;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerQuery;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilters;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;

public class BuglinkServiceImpl extends SierraServiceImpl implements
		BugLinkService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8719740627758175475L;

	public CreateCategoryResponse createCategory(
			final CreateCategoryRequest request) {
		return ConnectionFactory
				.withTransaction(new ServerQuery<CreateCategoryResponse>() {
					public CreateCategoryResponse perform(Query q, Server s) {
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
						final CreateCategoryResponse response = new CreateCategoryResponse();
						response.setSet(Categories.convert(sets.updateCategory(
								set, revision), s.getUid()));
						return response;
					}
				});
	}

	public ListCategoryResponse listCategories(ListCategoryRequest request) {
		return ConnectionFactory
				.withReadOnly(new ServerQuery<ListCategoryResponse>() {

					public ListCategoryResponse perform(Query q, Server s) {
						final ListCategoryResponse response = new ListCategoryResponse();
						final String server = s.getUid();
						final List<FilterSet> sets = response.getFilterSets();
						for (final CategoryDO set : new Categories(q)
								.listCategories()) {
							sets.add(Categories.convert(set, server));
						}
						return response;
					}
				});
	}

	public UpdateCategoryResponse updateCategory(
			final UpdateCategoryRequest request) throws RevisionException {
		return ConnectionFactory
				.withTransaction(new ServerQuery<UpdateCategoryResponse>() {
					public UpdateCategoryResponse perform(Query q, Server s) {
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
		return ConnectionFactory
				.withTransaction(new ServerQuery<CreateScanFilterResponse>() {
					public CreateScanFilterResponse perform(Query q, Server s) {
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
						final CreateScanFilterResponse response = new CreateScanFilterResponse();
						response.setFilter(ScanFilters.convert(filter, s
								.getUid()));
						return response;
					}
				});

	}

	public ListScanFilterResponse listScanFilters(ListScanFilterRequest request) {
		return ConnectionFactory
				.withReadOnly(new ServerQuery<ListScanFilterResponse>() {
					public ListScanFilterResponse perform(Query q, Server s) {
						final ListScanFilterResponse response = new ListScanFilterResponse();
						final List<ScanFilter> list = response.getScanFilter();
						for (final ScanFilterDO filter : new ScanFilters(q)
								.listScanFilters()) {
							list.add(ScanFilters.convert(filter, s.getUid()));
						}
						return response;
					}
				});
	}

	public UpdateScanFilterResponse updateScanFilter(
			final UpdateScanFilterRequest request) throws RevisionException {
		return ConnectionFactory
				.withTransaction(new ServerQuery<UpdateScanFilterResponse>() {
					public UpdateScanFilterResponse perform(Query q, Server s) {
						final UpdateScanFilterResponse response = new UpdateScanFilterResponse();
						response.setFilter(ScanFilters.convert(new ScanFilters(
								q).updateScanFilter(ScanFilters
								.convertDO(request.getFilter()), s
								.nextRevision()), s.getUid()));
						return response;
					}
				});
	}
}
