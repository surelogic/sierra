package com.surelogic.sierra.client.eclipse.model;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import com.surelogic.common.jdbc.*;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.client.eclipse.Data;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.SettingQueries;
import com.surelogic.sierra.jdbc.tool.FindingTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;

/**
 * A singleton that tracks the BugLink data in the client database
 * <p>
 * The class allows observers to changes to the BugLink data
 */
public final class BuglinkData extends DatabaseObservable<IBuglinkDataObserver> {

	private static final BuglinkData INSTANCE = new BuglinkData();

	static {
		DatabaseHub.getInstance().addObserver(INSTANCE);
	}

	public static BuglinkData getInstance() {
		return INSTANCE;
	}

	/**
	 * Needs to synchronize on this Key: uid of the category
	 */
	private Map<String, Set<String>> f_categories;
	/**
	 * Key: uid of the category
	 */
	private Map<String, CategoryDO> f_categoryDefs;
	/**
	 * Key: name of the finding type
	 */
	private Map<String, FindingTypeDO> f_findingTypes;

	private BuglinkData() {
		// singleton
	}

	@Override
	protected void notifyObserver(IBuglinkDataObserver o) {
		o.notify(this);
	}

	public void refresh() {
		Map<String, FindingTypeDO> findingTypes = new HashMap<String, FindingTypeDO>();
		Map<String, CategoryDO> categoryDefs = new HashMap<String, CategoryDO>();
		Map<String, Set<String>> categories = new HashMap<String, Set<String>>();
		try {
			Connection conn = Data.getInstance().readOnlyConnection();
			try {
				ConnectionQuery q = new ConnectionQuery(conn);
				FindingTypes ft = new FindingTypes(q);
				List<FindingTypeDO> ftypes = ft.listFindingTypes();
				for (FindingTypeDO ftype : ftypes) {
					findingTypes.put(ftype.getUid(), ftype);
				}

				List<CategoryDO> raw = Data.getInstance().withReadOnly(SettingQueries
						.getLocalCategories());
				// Key: uid of the category
				Map<String, CategoryDO> rawCategories = new HashMap<String, CategoryDO>(
						raw.size());

				for (CategoryDO c : raw) {
					rawCategories.put(c.getUid(), c);
					categoryDefs.put(c.getUid(), c);
				}
				for (CategoryDO c : raw) {
					String cat = c.getUid();
					computeCategory(categories, rawCategories, cat);
				}
			} finally {
				conn.close();
			}
			boolean notify = false;
			synchronized (this) {
				if (true) { // FIX check if equal?
					f_categories = categories;
					f_categoryDefs = categoryDefs;
					f_findingTypes = findingTypes;
					notify = true;
				}
			}
			if (notify)
				notifyObservers();
		} catch (SQLException e) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Unable to read the Buglink data from the database", e);
		}
	}

	/**
	 * Computes the finding types in each category
	 */
	private static Set<String> computeCategory(
			Map<String, Set<String>> categories,
			Map<String, CategoryDO> rawCategories, String uid) {
		Set<String> categoryTypes = categories.get(uid);
		if (categoryTypes == null) {
			// Compute which finding types are in the category
			CategoryDO defn = rawCategories.get(uid);
			if (defn == null) {
				SLLogger.getLogger().severe("No category def'n for " + uid);
				return Collections.emptySet();
			}
			categoryTypes = new HashSet<String>();
			for (String parent : defn.getParents()) {
				// categoryTypes.addAll(computeCategory(categories,
				// rawCategories, parent));
				for (String type : computeCategory(categories, rawCategories,
						parent)) {
					boolean added = categoryTypes.add(type);
					/*
					 * if (!added) { System.out.println(); } else {
					 * System.out.println(type); }
					 */
				}
			}
			for (CategoryEntryDO filter : defn.getFilters()) {
				if (filter.isFiltered()) {
					categoryTypes.remove(filter.getFindingType());
				} else {
					boolean added = categoryTypes.add(filter.getFindingType());
					/*
					 * if (!added) { System.out.println(); }
					 */
				}
			}
			categories.put(uid, categoryTypes);
		}
		return categoryTypes;
	}

	@Override
	public String toString() {
		/*
		 * Show the list of projects that we read from the database.
		 */
		synchronized (this) {
			return "[" + BuglinkData.class.getName() + "]";
		}
	}

	public synchronized Collection<String> getCategoryUids() {
		return f_categoryDefs.keySet();
	}

	/**
	 * @param name
	 *            Uid of the category
	 */
	public synchronized Collection<String> getFindingTypes(String uid) {
		return f_categories.get(uid);
	}

	/**
	 * @param name
	 *            Uid of the category
	 */
	public synchronized CategoryDO getCategoryDef(String uid) {
		return f_categoryDefs.get(uid);
	}

	/**
	 * @param findingType
	 *            Name of the finding type
	 */
	public synchronized FindingTypeDO getFindingType(String findingType) {
		return f_findingTypes.get(findingType);
	}

	/*
	 * Track changes to the database that can mutate the set of projects.
	 */
	@Override
	public void changed() {
		refresh();
	}
	/*
	 * @Override public void projectDeleted() { refresh(); }
	 * 
	 * @Override public void scanLoaded() { refresh(); }
	 */
}
