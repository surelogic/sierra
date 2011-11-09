package com.surelogic.sierra.jdbc.settings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a view of an acyclic, linked group of categories from a single
 * node in the graph.
 * 
 * @author nathan
 * 
 */
public class CategoryGraph {

	private final CategoryDO cat;
	private final Map<String, CategoryDO> categories;

	/**
	 * 
	 * @param cat
	 *            the category in the node that we are at
	 * @param categories
	 *            a map of all categories in the graph
	 */
	CategoryGraph(CategoryDO cat, Map<String, CategoryDO> categories) {
		this.cat = cat;
		this.categories = categories;
	}

	Set<CategoryGraph> getParents() {
		final Set<CategoryGraph> set = new HashSet<CategoryGraph>();
		for (final String uid : cat.getParents()) {
			set.add(new CategoryGraph(categories.get(uid), categories));
		}
		return set;
	}

	/**
	 * Provides the set of finding types in the current category
	 * 
	 * @return a set of finding type uids
	 */
	public Set<String> getFindingTypes() {
		final Set<String> set = new HashSet<String>();
		for (final CategoryGraph c : getParents()) {
			set.addAll(c.getFindingTypes());
		}
		for (final CategoryEntryDO e : cat.getFilters()) {
			if (e.isFiltered()) {
				set.remove(e.getFindingType());
			} else {
				set.add(e.getFindingType());
			}
		}
		return set;
	}

	/**
	 * Return the category uid
	 * 
	 * @return
	 */
	public String getUid() {
		return cat.getUid();
	}
}
