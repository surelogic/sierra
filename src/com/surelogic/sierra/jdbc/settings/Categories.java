package com.surelogic.sierra.jdbc.settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;

/**
 * This class implements data access for {@link CategoryDO} objects. A Category
 * represents a collection of finding types. It is defined in terms of a set of
 * parent categories, and a set of finding types to include/exclude. The finding
 * types present in a category consist of the union of the finding types in the
 * parent categories masked by the set of finding types to include/exclude.
 * 
 * Categories can be created, updated, and deleted.
 * 
 * @author nathan
 * 
 */
public final class Categories {

	Query q;

	public Categories(Query q) {
		this.q = q;
	}

	/**
	 * List all of the categories owned by a particular server
	 * 
	 * @param server
	 *            a server uid. May not be null.
	 * @return
	 */
	public List<CategoryDO> listServerCategories(String server) {
		if (server == null) {
			throw new IllegalArgumentException("server may not be null");
		}
		final List<CategoryDO> list = new ArrayList<CategoryDO>();
		for (final String uid : q.prepared("FilterSets.listServerFilterSets",
				new StringRowHandler()).call(server)) {
			list.add(getCategory(uid));
		}
		return list;
	}

	/**
	 * List all available filter sets residing in the system.
	 * 
	 * @return
	 */
	public List<CategoryDO> listCategories() {
		final List<CategoryDO> list = new ArrayList<CategoryDO>();
		for (final String uid : q.prepared("FilterSets.listFilterSets",
				new StringRowHandler()).call()) {
			list.add(getCategory(uid));
		}
		return list;
	}

	/**
	 * Return a Filter Set data object with the given uid.
	 * 
	 * @param uid
	 * @return
	 * @throws SQLException
	 */
	public CategoryDO getCategory(final String uid) {
		final FilterSetRecord rec = q.record(FilterSetRecord.class);
		rec.setUid(uid);
		if (rec.select()) {
			final CategoryDO set = new CategoryDO();
			set.setId(rec.getId());
			set.setInfo(rec.getInfo());
			set.setName(rec.getName());
			set.setRevision(rec.getRevision());
			set.setUid(rec.getUid());
			set.getFilters().addAll(
					q.prepared("FilterSets.listFilterSetFilters",
							new RowHandler<CategoryEntryDO>() {
								public CategoryEntryDO handle(Row r) {
									return new CategoryEntryDO(r.nextString(),
											"Y".equals(r.nextString()));
								}
							}).call(rec.getId()));
			set.getParents().addAll(
					q.prepared("FilterSets.listFilterSetParents",
							new StringRowHandler()).call(rec.getId()));
			return set;
		} else {
			return null;
		}
	}

	/**
	 * Return a graph of this category and all of its dependencies.
	 * 
	 * @param uid
	 *            a category uid
	 * @return a CategoryGraph if the category exists <code>null</code>
	 *         otherwise
	 */
	public CategoryGraph getCategoryGraph(String uid) {
		final CategoryDO cat = getCategory(uid);
		if (cat != null) {
			final Map<String, CategoryDO> map = new HashMap<String, CategoryDO>();
			graphHelper(map, cat);
			return new CategoryGraph(cat, map);
		} else {
			return null;
		}
	}

	/**
	 * Return a graph of this category and all of its dependencies.
	 * 
	 * @param uid
	 *            a category uid
	 * @return an in-order list of CategoryGraph objects. References may be
	 *         <code>null</code> if a category does not exist.
	 */
	public List<CategoryGraph> getCategoryGraphs(List<String> uids) {
		final Map<String, CategoryDO> map = new HashMap<String, CategoryDO>();
		final List<CategoryGraph> graphs = new ArrayList<CategoryGraph>(uids
				.size());
		for (final String uid : uids) {
			final CategoryDO cat = getCategory(uid);
			if (cat != null) {
				graphHelper(map, cat);
				graphs.add(new CategoryGraph(cat, map));
			} else {
				graphs.add(null);
			}
		}
		return graphs;
	}

	/*
	 * Add this category and all of its dependencies to the map
	 */
	private void graphHelper(Map<String, CategoryDO> map, CategoryDO cat) {
		map.put(cat.getUid(), cat);
		for (final String uid : cat.getParents()) {
			if (map.get(uid) == null) {
				graphHelper(map, getCategory(uid));
			}
		}
	}

	/**
	 * Create an empty filter set.
	 * 
	 * @param name
	 * @param description
	 *            an (optional) brief description of the filter set
	 * @param revision
	 * @return the uid of this filter set
	 */
	public CategoryDO createCategory(String name, String description,
			long revision) {
		if ((name == null) || (name.length() == 0) || (name.length() > 2000)) {
			throw new IllegalArgumentException(name + " is not a valid name");
		}
		final FilterSetRecord filterSetRec = q.record(FilterSetRecord.class);
		filterSetRec.setName(name);
		filterSetRec.setInfo(description);
		filterSetRec.setRevision(revision);
		filterSetRec.setUid(UUID.randomUUID().toString());
		filterSetRec.insert();
		return getCategory(filterSetRec.getUid());
	}

	/**
	 * Writes a filter set to the database, overwriting any local copy. This
	 * should be used solely when updating filter sets owned by another server.
	 * 
	 * @param set
	 */
	public void writeCategory(CategoryDO set) {
		/*
		 * Essentially the same as update, but w/o a revision check or failure
		 * if it doesn't exist.
		 */
		final FilterSetRecord rec = q.record(FilterSetRecord.class);
		rec.setUid(set.getUid());
		if (rec.select()) {
			checkCyclicDependences(set.getParents(), set.getUid());
			rec.setRevision(set.getRevision());
			rec.setName(set.getName());
			rec.setInfo(set.getInfo());
			rec.update();
		} else {
			rec.setRevision(set.getRevision());
			rec.setName(set.getName());
			rec.setInfo(set.getInfo());
			rec.insert();
		}
		q.prepared("FilterSets.deleteFilterSetParents").call(rec.getId());
		final Queryable<Void> insertParent = q
				.prepared("FilterSets.insertFilterSetParent");
		final FilterSetRecord parentRec = q.record(FilterSetRecord.class);
		for (final String parent : set.getParents()) {
			parentRec.setUid(parent);
			if (parentRec.select()) {
				insertParent.call(rec.getId(), parentRec.getId());
			} else {
				throw new IllegalArgumentException("The specified parent uid "
						+ parent + " does not match an existing filter set.");
			}
		}
		q.prepared("FilterSets.deleteFilterSetEntries").call(rec.getId());
		final Queryable<Void> insertEntry = q
				.prepared("FilterSets.insertFilterSetEntry");
		for (final CategoryEntryDO entry : set.getFilters()) {
			insertEntry.call(rec.getId(), entry.getFindingType(), entry
					.isFiltered() ? "Y" : "N");
		}
	}

	/**
	 * Update a filter set. This should only be done by the server that owns the
	 * category. Any one else should call {@link #writeCategory(CategoryDO)},
	 * and then request that the owning server be updated when they are ready to
	 * commit their local changes.
	 * 
	 * @param set
	 * @param revision
	 * @return
	 * @throws RevisionException
	 *             if the filter set has been modified since it was last read.
	 * @throws IllegalArgumentException
	 *             if the filter set would in fact create a cyclic graph.
	 */
	public CategoryDO updateCategory(CategoryDO set, long revision) {
		final FilterSetRecord rec = q.record(FilterSetRecord.class);
		rec.setUid(set.getUid());
		if (rec.select()) {
			if (rec.getRevision() == set.getRevision()) {
				checkCyclicDependences(set.getParents(), set.getUid());
				rec.setRevision(revision);
				rec.setName(set.getName());
				rec.setInfo(set.getInfo());
				rec.update();
				q.prepared("FilterSets.deleteFilterSetParents").call(
						rec.getId());
				final Queryable<Void> insertParent = q
						.prepared("FilterSets.insertFilterSetParent");
				final FilterSetRecord parentRec = q
						.record(FilterSetRecord.class);
				for (final String parent : set.getParents()) {
					parentRec.setUid(parent);
					if (parentRec.select()) {
						insertParent.call(rec.getId(), parentRec.getId());
					} else {
						throw new IllegalArgumentException(
								"The specified parent uid "
										+ parent
										+ " does not match an existing filter set.");
					}
				}
				q.prepared("FilterSets.deleteFilterSetEntries").call(
						rec.getId());
				final Queryable<Void> insertEntry = q
						.prepared("FilterSets.insertFilterSetEntry");
				for (final CategoryEntryDO entry : set.getFilters()) {
					insertEntry.call(rec.getId(), entry.getFindingType(), entry
							.isFiltered() ? "Y" : "N");
				}
				return getCategory(set.getUid());
			} else {
				throw new RevisionException(
						"Filter set revision did not match the current revision for "
								+ set.getUid());
			}
		} else {
			throw new IllegalArgumentException("No filter set with uuid "
					+ set.getUid() + " exists.");
		}
	}

	/**
	 * Checks to make sure that adding the given parent uids will not introduce
	 * a cyclic dependency. This method is called anytime new edges are added to
	 * our graph. As long as the graph is valid before the edges are added,
	 * checkCyclicDependencies will ensure that the same is true afterwards.
	 * 
	 * @param uids
	 *            the parent uids
	 * @param filterSetUid
	 *            the filter set uid
	 * @throws IllegalArgumentException
	 */
	private void checkCyclicDependences(Collection<String> uids,
			String filterSetUid) {
		for (final String uid : uids) {
			if (!uid.equals(filterSetUid)) {
				checkCyclicDependences(q.prepared("FilterSets.findParents",
						new StringRowHandler()).call(uid), filterSetUid);
			} else {
				throw new IllegalArgumentException(
						"Proposed filter Set contains a cyclic dependency.");
			}
		}
	}

	/**
	 * Delete an existing category
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteCategory(String uid) {
		final FilterSetRecord set = q.record(FilterSetRecord.class);
		set.setUid(uid);
		if (set.select()) {
			if (q.prepared("FilterSets.listFilterSetChildren",
					new StringRowHandler()).call(set.getId()).isEmpty()) {
				set.delete();
			} else {
				throw new IllegalArgumentException(
						"Can not delete filter set with uid " + uid
								+ " because it has children.");
			}
		} else {
			throw new IllegalArgumentException("No filter set with the uid "
					+ uid + " exists.");
		}
	}

	/**
	 * Delete an existing category
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteCategories(Collection<String> uids) {
		final FilterSetRecord set = q.record(FilterSetRecord.class);
		for (final String uid : uids) {
			set.setUid(uid);
			if (set.select()) {
				if (uids.containsAll((q.prepared(
						"FilterSets.listFilterSetChildren",
						new StringRowHandler()).call(set.getId())))) {
					set.delete();
				} else {
					throw new IllegalArgumentException(
							"Can not delete filter set with uid " + uid
									+ " because it has children.");
				}
			} else {
				throw new IllegalArgumentException(
						"No filter set with the uid " + uid + " exists.");
			}
		}
	}

	/**
	 * Produce a {@link FilterSet} backed by the provided {@link CategoryDO} and
	 * owned by the given server
	 * 
	 * @param in
	 * @param server
	 *            a server uid
	 * @return
	 */
	public static FilterSet convert(CategoryDO in, String server) {
		final FilterSet out = new FilterSet();
		out.setName(in.getName());
		out.setOwner(server);
		out.setRevision(in.getRevision());
		out.setUid(in.getUid());
		out.setInfo(in.getInfo());
		final List<FilterEntry> entries = out.getFilter();
		for (final CategoryEntryDO entry : in.getFilters()) {
			final FilterEntry e = new FilterEntry();
			e.setFiltered(entry.isFiltered());
			e.setType(entry.getFindingType());
			entries.add(e);
		}
		out.getParent().addAll(in.getParents());
		return out;
	}

	/**
	 * Produce a {@link CategoryDO} from the provided {@link FilterSet}.
	 * 
	 * @param in
	 * @return
	 */
	public static CategoryDO convertDO(FilterSet in) {
		final CategoryDO out = new CategoryDO();
		out.setInfo(in.getInfo());
		out.setName(in.getName());
		out.setRevision(in.getRevision());
		out.setUid(in.getUid());
		final Set<CategoryEntryDO> entries = out.getFilters();
		for (final FilterEntry e : in.getFilter()) {
			entries.add(new CategoryEntryDO(e.getType(), e.isFiltered()));
		}
		out.getParents().addAll(in.getParent());
		return out;
	}
}
