package com.surelogic.sierra.jdbc.settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Queryable;
import com.surelogic.sierra.jdbc.Row;
import com.surelogic.sierra.jdbc.RowHandler;
import com.surelogic.sierra.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;

public class FilterSets {

	Query q;

	public FilterSets(Query q) {
		this.q = q;
	}

	/**
	 * List all available filter sets residing in the system.
	 * 
	 * @return
	 */
	public List<FilterSetDO> listFilterSets() {
		final List<FilterSetDO> list = new ArrayList<FilterSetDO>();
		for (final String uid : q.prepared("FilterSets.listFilterSets",
				new StringRowHandler()).call()) {
			list.add(getFilterSet(uid));
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
	public FilterSetDO getFilterSet(final String uid) {
		final FilterSetRecord rec = q.record(FilterSetRecord.class);
		rec.setUid(uid);
		if (rec.select()) {
			final FilterSetDO set = new FilterSetDO();
			set.setInfo(rec.getInfo());
			set.setName(rec.getName());
			set.setRevision(rec.getRevision());
			set.setUid(rec.getUid());
			set.getFilters().addAll(
					q.prepared("FilterSets.listFilterSetFilters",
							new RowHandler<FilterEntryDO>() {
								public FilterEntryDO handle(Row r) {
									return new FilterEntryDO(r.nextString(),
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
	 * Create an empty filter set.
	 * 
	 * @param name
	 * @param description
	 * @param revision
	 *            an (optional) brief description of the filter set
	 * @return the uid of this filter set
	 * @throws SQLException
	 */
	public FilterSetDO createFilterSet(String name, String description,
			long revision) {
		final FilterSetRecord filterSetRec = q.record(FilterSetRecord.class);
		filterSetRec.setName(name);
		filterSetRec.setInfo(description);
		filterSetRec.setRevision(revision);
		filterSetRec.setUid(UUID.randomUUID().toString());
		filterSetRec.insert();
		return getFilterSet(filterSetRec.getUid());
	}

	/**
	 * Writes a filter set to the database, overwriting any local copy. This
	 * should be used solely when updating filter sets owned by another server.
	 * 
	 * @param set
	 */
	public void writeFilterSet(FilterSetDO set) {
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
		for (final FilterEntryDO entry : set.getFilters()) {
			insertEntry.call(rec.getId(), entry.getFindingType(), entry
					.isFiltered() ? "Y" : "N");
		}
	}

	/**
	 * Update a filter set.
	 * 
	 * @param set
	 * @param revision
	 * @return
	 * @throws RevisionException
	 *             if the filter set has been modified since it was last read.
	 * @throws IllegalArgumentException
	 *             if the filter set would in fact create a cyclic graph.
	 */
	public FilterSetDO updateFilterSet(FilterSetDO set, long revision) {
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
				for (final FilterEntryDO entry : set.getFilters()) {
					insertEntry.call(rec.getId(), entry.getFindingType(), entry
							.isFiltered() ? "Y" : "N");
				}
				return getFilterSet(set.getUid());
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
	 * a cyclic dependency
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
	 * Delete an existing filter set.
	 * 
	 * @param uid
	 * @throws SQLException
	 */
	public void deleteFilterSet(String uid) {
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

	// TODO Find a place for message conversion logic
	public static FilterSet convert(FilterSetDO in, String server) {
		final FilterSet out = new FilterSet();
		out.setName(in.getName());
		out.setOwner(server);
		out.setRevision(in.getRevision());
		out.setUid(in.getUid());
		out.setInfo(in.getInfo());
		final List<FilterEntry> entries = out.getFilter();
		for (final FilterEntryDO entry : in.getFilters()) {
			final FilterEntry e = new FilterEntry();
			e.setFiltered(entry.isFiltered());
			e.setType(entry.getFindingType());
			entries.add(e);
		}
		out.getParent().addAll(in.getParents());
		return out;
	}

	public static FilterSetDO convertDO(FilterSet in) {
		final FilterSetDO out = new FilterSetDO();
		out.setInfo(in.getInfo());
		out.setName(in.getName());
		out.setRevision(in.getRevision());
		out.setUid(in.getUid());
		final Set<FilterEntryDO> entries = out.getFilters();
		for (final FilterEntry e : in.getFilter()) {
			entries.add(new FilterEntryDO(e.getType(), e.isFiltered()));
		}
		out.getParents().addAll(in.getParent());
		return out;
	}
}
