package com.surelogic.sierra.jdbc.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.DBQueryNoResult;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.message.BugLinkServiceClient;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.ListCategoryRequest;
import com.surelogic.sierra.tool.message.ListCategoryResponse;
import com.surelogic.sierra.tool.message.ListScanFilterRequest;
import com.surelogic.sierra.tool.message.ListScanFilterResponse;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * This class represents implementations of the client-side behavior of the bug
 * link service.
 * 
 * @author nathan
 * 
 */
public class SettingQueries {

	public static final String GLOBAL_UUID = "de3034ec-65d5-4d4a-b059-1adf8fc7b12d";
	public static final String GLOBAL_NAME = "GLOBAL";

	/**
	 * Return a view of the provided scan filter
	 * 
	 * @param scanFilter
	 * @return
	 */
	public static DBQuery<ScanFilterView> scanFilterFor(
			final ScanFilterDO scanFilter) {
		return new DBQuery<ScanFilterView>() {
			public ScanFilterView perform(Query q) {
				return scanFilterView(q, scanFilter);
			}
		};
	}

	public static final DBQuery<ListCategoryRequest> categoryRequest() {
		return new DBQuery<ListCategoryRequest>() {
			public ListCategoryRequest perform(Query q) {
				final ListCategoryRequest r = new ListCategoryRequest();
				r.getServerRevisions().addAll(
						q.statement("FilterSets.latestServerRevisions",
								new ServerRevisionRowHandler()).call());
				return r;
			}
		};
	}

	/**
	 * Queries the specified server for a list of categories, and returns a
	 * {@link DBQuery} that, when run, will write the given categories into the
	 * database. Categories that are of a lower revision than the local copy
	 * will not be overwritten.
	 * 
	 * @param loc
	 * @return
	 */
	public static final DBQuery<ListCategoryResponse> retrieveCategories(
			SierraServerLocation loc, ListCategoryRequest request) {
		final ListCategoryResponse response = BugLinkServiceClient.create(loc)
				.listCategories(request);
		return updateCategories(response, true);
	}

	/**
	 * Queries the specified server for a list of categories, and returns a
	 * {@link DBQuery} that, when run, will return a list of categories that
	 * would have been updated. Categories that are of a lower revision than the
	 * local copy will not be returned.
	 */
	public static final DBQuery<ListCategoryResponse> getNewCategories(
			SierraServerLocation loc, ListCategoryRequest request) {
		final ListCategoryResponse response = BugLinkServiceClient.create(loc)
				.listCategories(request);
		return updateCategories(response, false);
	}

	public static final DBQuery<ListCategoryResponse> updateCategories(
			final ListCategoryResponse response, final boolean update) {
		return new DBQuery<ListCategoryResponse>() {
			public ListCategoryResponse perform(Query q) {
				final Categories sets = new Categories(q);
				final Queryable<Void> delete = update ? q
						.prepared("Definitions.deleteDefinition") : null;
				final Queryable<Void> insert = update ? q
						.prepared("Definitions.insertDefinition") : null;
				// for (final FilterSet set : response.getFilterSets()) {
				final Iterator<FilterSet> it = response.getFilterSets()
						.iterator();
				while (it.hasNext()) {
					final FilterSet set = it.next();
					final String uid = set.getUid();
					final CategoryDO c = sets.getCategory(uid);
					if (c == null) {
						if (update) {
							insert.call(uid, set.getOwner());
							sets.writeCategory(Categories.convertDO(set));
						}
					} else if (c.getRevision() < set.getRevision()) {
						if (update) {
							delete.call(uid);
							insert.call(uid, set.getOwner());
							sets.writeCategory(Categories.convertDO(set));
						}
					} else if (!update) {
						it.remove(); // Remove since it's older
					}
				}
				if (update) {
					sets.deleteCategories(response.getDeletions());
				}
				return response;
			}
		};
	}

	public static final DBQuery<List<CategoryDO>> getLocalCategories() {
		return new DBQuery<List<CategoryDO>>() {
			public List<CategoryDO> perform(Query q) {
				final Categories sets = new Categories(q);
				return sets.listCategories();
			}
		};
	}

	public static final DBQuery<ListScanFilterRequest> scanFilterRequest() {
		return new DBQuery<ListScanFilterRequest>() {
			public ListScanFilterRequest perform(Query q) {
				final ListScanFilterRequest r = new ListScanFilterRequest();
				r.getServerRevisions().addAll(
						q.statement("ScanFilters.latestServerRevisions",
								new ServerRevisionRowHandler()).call());
				return r;
			}
		};
	}

	public static final DBQuery<ListScanFilterResponse> retrieveScanFilters(
			SierraServerLocation loc, ListScanFilterRequest request) {
		return getScanFilters(loc, request, true);
	}

	public static final DBQuery<ListScanFilterResponse> getNewScanFilters(
			SierraServerLocation loc, ListScanFilterRequest request) {
		return getScanFilters(loc, request, false);
	}

	private static final DBQuery<ListScanFilterResponse> getScanFilters(
			SierraServerLocation loc, ListScanFilterRequest request,
			final boolean update) {
		final ListScanFilterResponse response = BugLinkServiceClient
				.create(loc).listScanFilters(request);
		return new DBQuery<ListScanFilterResponse>() {
			public ListScanFilterResponse perform(Query q) {
				final ScanFilters filters = new ScanFilters(q);
				final Queryable<Void> delete = update ? q
						.prepared("Definitions.deleteDefinition") : null;
				final Queryable<Void> insert = update ? q
						.prepared("Definitions.insertDefinition") : null;
				// for (final ScanFilter filter : response.getScanFilter()) {
				final Iterator<ScanFilter> it = response.getScanFilter()
						.iterator();
				while (it.hasNext()) {
					final ScanFilter filter = it.next();
					final String uid = filter.getUid();
					final ScanFilterDO f = filters.getScanFilter(uid);
					if (f == null) {
						if (update) {
							insert.call(uid, filter.getOwner());
							filters.writeScanFilter(ScanFilters
									.convertDO(filter));
						}
					} else if (f.getRevision() < filter.getRevision()) {
						if (update) {
							delete.call(uid);
							insert.call(uid, filter.getOwner());
							filters.writeScanFilter(ScanFilters
									.convertDO(filter));
						}
					} else if (!update) {
						it.remove(); // Remove since it's older
					}
				}
				if (update) {
					for (final String uid : response.getDeletions()) {
						filters.deleteScanFilter(uid);
					}
				}
				return response;
			}
		};
	}

	public static final DBQuery<List<ScanFilterDO>> getLocalScanFilters() {
		return new DBQuery<List<ScanFilterDO>>() {
			public List<ScanFilterDO> perform(Query q) {
				final ScanFilters filters = new ScanFilters(q);
				return filters.listScanFilters();
			}
		};
	}

	/**
	 * Generates a filter to be used when when filtering artifacts from a scan
	 * and assigning importance to findings during a scan publish action.
	 * 
	 * @param uid
	 * @return
	 */
	public static final DBQuery<ScanFilterView> scanFilterForUid(
			final String uid) {
		return new DBQuery<ScanFilterView>() {
			public ScanFilterView perform(Query q) {
				final ScanFilters filters = new ScanFilters(q);
				final ScanFilterDO sf = filters.getScanFilter(uid);
				if (sf == null) {
					throw new IllegalArgumentException(
							"No scan filter with uid " + uid + " exists");
				}
				return scanFilterView(q, sf);
			}

		};
	}

	/**
	 * Generates a filter to be used when when filtering artifacts from a scan
	 * and assigning importance to findings during a scan publish action.
	 * 
	 * @param projectName
	 * @return
	 */
	public static final DBQuery<ScanFilterView> scanFilterForProject(
			final String projectName) {
		return new DBQuery<ScanFilterView>() {
			public ScanFilterView perform(Query q) {
				final ScanFilters filters = new ScanFilters(q);
				ScanFilterDO sf = filters.getScanFilterByProject(projectName);
				if (sf == null) {
					sf = filters.getScanFilter(GLOBAL_UUID);
				}
				return scanFilterView(q, sf);
			}
		};
	}

	/**
	 * Update the global filter set locally. Do not increment the revision.
	 * 
	 * @param uids
	 *            a list of finding type identifiers to include in scans
	 * @return
	 */
	public static DBQueryNoResult updateGlobalFilterSet(
			final Collection<String> uids) {
		return new DBQueryNoResult() {
			@Override
			public void doPerform(Query q) {
				final ScanFilters s = new ScanFilters(q);
				final ScanFilterDO scanFilter = s.getScanFilter(GLOBAL_UUID);
				final Set<TypeFilterDO> types = scanFilter.getFilterTypes();
				types.clear();
				for (final String uid : uids) {
					types.add(new TypeFilterDO(uid, null, false));
				}
				s.writeScanFilter(scanFilter);
			}
		};
	}

	private static ScanFilterView scanFilterView(Query q, ScanFilterDO sf) {
		final Categories cats = new Categories(q);
		final FindingTypes types = new FindingTypes(q);
		final Set<CategoryFilterDO> catFilters = sf.getCategories();
		final List<String> catFilterUids = new ArrayList<String>(catFilters
				.size());
		for (final CategoryFilterDO cat : catFilters) {
			catFilterUids.add(cat.getUid());
		}
		final Map<String, CategoryGraph> catGraphs = new HashMap<String, CategoryGraph>(
				catFilters.size());
		final Set<String> typeSet = new HashSet<String>(catFilters.size() * 10);
		for (final CategoryGraph graph : cats.getCategoryGraphs(catFilterUids)) {
			catGraphs.put(graph.getUid(), graph);
			typeSet.addAll(graph.getFindingTypes());
		}
		for (final TypeFilterDO typeFilter : sf.getFilterTypes()) {
			typeSet.add(typeFilter.getFindingType());
		}
		final Map<String, FindingTypeDO> typeMap = new HashMap<String, FindingTypeDO>(
				typeSet.size());
		for (final String type : typeSet) {
			typeMap.put(type, types.getFindingType(type));
		}
		return new ScanFilterView(sf, typeMap, catGraphs);
	}

	/**
	 * The full location, on the Java classpath, of the default world file.
	 */
	public static final String DEFAULT_FILTER_SET_FILE = "/com/surelogic/sierra/jdbc/settings/SureLogicDefaultFilterSet.txt";

	/**
	 * Gets the default set of finding type UUIDs that have been selected by
	 * SureLogic to be filtered out of scans.
	 * 
	 * @return the SureLogic default filter set.
	 */
	public static Set<String> getSureLogicDefaultScanFilters() {

		final Set<String> types = new HashSet<String>();
		final URL defaultURL = Thread.currentThread().getContextClassLoader()
				.getResource(DEFAULT_FILTER_SET_FILE);
		if (defaultURL == null) {
			SLLogger.getLogger().log(
					Level.WARNING,
					"Unable to find the SureLogic default filter set file "
							+ DEFAULT_FILTER_SET_FILE + " on the classpath.");
			return types;
		}
		try {
			final BufferedReader in = new BufferedReader(new InputStreamReader(
					defaultURL.openStream()));
			try {
				String s = in.readLine();
				while (s != null) {
					s = s.trim();
					if (!s.startsWith("--") && !"".equals(s)) {
						types.add(s);
					}
					s = in.readLine();
				}
			} finally {
				in.close();
			}
		} catch (final IOException e) {
			SLLogger.getLogger().log(
					Level.WARNING,
					"IO failure reading the SureLogic default filter set file "
							+ DEFAULT_FILTER_SET_FILE, e);
		}
		return types;
	}
}
