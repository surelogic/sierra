package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.tool.message.CategoryFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.TypeFilter;

/**
 * This class implements data access for {@link ScanFilterDO} objects. A scan
 * filter represents a collection of finding types that should be included when
 * processing the output of a scan, and the importance that each should be
 * given. Finding types can be included singly, or a whole category can be
 * included and assigned an importance. Finding types can also be excluded if,
 * for instance, some finding types in a category should not be included.
 * 
 * Categories can be created, updated, and deleted.
 * 
 * @author nathan
 * @see ScanFilterDO
 */
public class ScanFilters {

	private final Query q;

	public ScanFilters(Query q) {
		this.q = q;
	}

	public ScanFilters(Connection conn) {
		q = new ConnectionQuery(conn);
	}

	/**
	 * List all available scan filters.
	 * 
	 * @return
	 */
	public List<ScanFilterDO> listScanFilters() {
		final List<ScanFilterDO> list = new ArrayList<ScanFilterDO>();
		for (final String s : q.prepared("ScanFilters.listScanFilters",
				new StringRowHandler()).call()) {
			list.add(getScanFilter(s));
		}
		return list;
	}

	/**
	 * Create a new scan filter. Nothing is included in the scan filter.
	 * 
	 * @param name
	 * @param revision
	 * @return the newly created scan filter
	 */
	public ScanFilterDO createScanFilter(String name, long revision) {
		if ((name == null) || (name.length() == 0) || (name.length() > 255)) {
			throw new IllegalArgumentException(name
					+ " is not a valid scan filter name.");
		}
		final ScanFilterRecord settingsRec = q.record(ScanFilterRecord.class);
		settingsRec.setName(name);
		settingsRec.setRevision(revision);
		settingsRec.setUid(UUID.randomUUID().toString());
		settingsRec.insert();
		return getScanFilter(settingsRec.getUid());
	}

	/**
	 * Update an existing scan filter. This should only be done by the server
	 * that owns the category. Any one else should call
	 * {@link #writeScanFilter(ScanFilterDO)} and then request that the owning
	 * server be updated when they are ready to commit their local changes.
	 * 
	 * @param settings
	 * @param revision
	 * @return
	 * @throws RevisionException
	 *             if the revision in the database does not match the expected
	 *             revision
	 */
	public ScanFilterDO updateScanFilter(ScanFilterDO settings, long revision) {
		final ScanFilterRecord settingsRec = q.record(ScanFilterRecord.class);
		settingsRec.setUid(settings.getUid());
		if (settingsRec.select()) {
			if (settingsRec.getRevision() == settings.getRevision()) {
				settingsRec.setName(settings.getName());
				settingsRec.setRevision(revision);
				settingsRec.update();
				q.prepared("ScanFilters.deleteTypeFilters").call(
						settingsRec.getId());
				q.prepared("ScanFilters.deleteCategoryFilters").call(
						settingsRec.getId());
				final Queryable<Void> insertTypeFilter = q
						.prepared("ScanFilters.insertTypeFilter");
				final Queryable<Void> insertCategoryFilter = q
						.prepared("ScanFilters.insertCategoryFilter");
				final Queryable<Void> deleteProjectRelation = q
						.prepared("ScanFilters.deleteProjectRelation");
				final Queryable<Void> insertProject = q
						.prepared("ScanFilters.insertProject");
				for (final String project : settings.getProjects()) {
					deleteProjectRelation.call(project);
					insertProject.call(project);
				}
				for (final CategoryFilterDO cat : settings.getCategories()) {
					insertCategoryFilter.call(settingsRec.getId(),
							cat.getUid(),
							cat.getImportance() == null ? Nulls.STRING : cat
									.getImportance());
				}
				for (final TypeFilterDO type : settings.getFilterTypes()) {
					insertTypeFilter.call(settingsRec.getId(), type
							.getFindingType(),
							type.getImportance() == null ? Nulls.INT : type
									.getImportance().ordinal(), type
									.isFiltered());
				}
				return getScanFilter(settingsRec.getUid());
			} else {
				throw new RevisionException();
			}
		} else {
			throw new IllegalArgumentException("No settings with uid "
					+ settings.getUid() + " exist.");
		}
	}

	/**
	 * Write a scan filter into the local database, over an existing filter if
	 * necessary.
	 * 
	 * @param settings
	 */
	public void writeScanFilter(ScanFilterDO settings) {
		final ScanFilterRecord settingsRec = q.record(ScanFilterRecord.class);
		settingsRec.setUid(settings.getUid());
		if (settingsRec.select()) {
			settingsRec.setName(settings.getName());
			settingsRec.setRevision(settings.getRevision());
			settingsRec.update();
		} else {
			settingsRec.setUid(settings.getUid());
			settingsRec.setName(settings.getName());
			settingsRec.setRevision(settings.getRevision());
			settingsRec.insert();
		}
		q.prepared("ScanFilters.deleteTypeFilters").call(settingsRec.getId());
		q.prepared("ScanFilters.deleteCategoryFilters").call(
				settingsRec.getId());
		final Queryable<Void> insertTypeFilter = q
				.prepared("ScanFilters.insertTypeFilter");
		final Queryable<Void> insertCategoryFilter = q
				.prepared("ScanFilters.insertCategoryFilter");
		final Queryable<Void> deleteProjectRelation = q
				.prepared("ScanFilters.deleteProjectRelation");
		final Queryable<Void> insertProject = q
				.prepared("ScanFilters.insertProject");
		for (final String project : settings.getProjects()) {
			deleteProjectRelation.call(project);
			insertProject.call(settingsRec.getId(), project);
		}
		for (final CategoryFilterDO cat : settings.getCategories()) {
			insertCategoryFilter.call(settingsRec.getId(), cat.getUid(), cat
					.getImportance() == null ? Nulls.STRING : cat
					.getImportance());
		}
		for (final TypeFilterDO type : settings.getFilterTypes()) {
			insertTypeFilter.call(settingsRec.getId(), type.getFindingType(),
					type.getImportance() == null ? Nulls.INT : type
							.getImportance().ordinal(), type.isFiltered());
		}
	}

	/**
	 * Return the relevant {@link ScanFilterDO}
	 * 
	 * @param uid
	 * @return
	 */
	public ScanFilterDO getScanFilter(String uid) {
		final ScanFilterRecord settingsRec = q.record(ScanFilterRecord.class);
		settingsRec.setUid(uid);
		if (settingsRec.select()) {
			final ScanFilterDO settings = new ScanFilterDO();
			settings.setUid(settingsRec.getUid());
			settings.setName(settingsRec.getName());
			settings.setRevision(settingsRec.getRevision());
			settings.getProjects().addAll(
					q.prepared("ScanFilters.listProjects",
							new StringRowHandler()).call(uid));
			settings.getCategories().addAll(
					q.prepared("ScanFilters.listFilterSets",
							new FilterSetHandler()).call(uid));
			settings.getFilterTypes().addAll(
					q.prepared("ScanFilters.listFilters", new FilterHandler())
							.call(uid));
			return settings;
		} else {
			return null;
		}
	}

	/**
	 * Get the scan filter associated with a given project.
	 * 
	 * @param project
	 * @return
	 */
	public ScanFilterDO getScanFilterByProject(String project) {
		if (project == null) {
			throw new IllegalArgumentException("Project may not be null.");
		}
		final String uid = q.prepared("ScanFilters.selectByProject",
				SingleRowHandler.from(new StringRowHandler())).call(project);
		return uid == null ? null : getScanFilter(uid);
	}

	private static class FilterSetHandler implements
			RowHandler<CategoryFilterDO> {

		public CategoryFilterDO handle(Row r) {
			return new CategoryFilterDO(r.nextString(), toImportance(r
					.nextString()));
		}
	}

	private static class FilterHandler implements RowHandler<TypeFilterDO> {

		public TypeFilterDO handle(Row r) {
			return new TypeFilterDO(r.nextString(),
					toImportance(r.nextString()), r.nextBoolean());
		}
	}

	private static Importance toImportance(String imp) {
		return imp == null ? null : Importance.fromValue(imp);
	}

	public static ScanFilterDO convertDO(ScanFilter message) {
		final ScanFilterDO filter = new ScanFilterDO();
		filter.setName(message.getName());
		filter.setUid(message.getUid());
		filter.setRevision(message.getRevision());
		final Set<CategoryFilterDO> cSet = filter.getCategories();
		for (final CategoryFilter c : message.getCategoryFilter()) {
			cSet.add(new CategoryFilterDO(c.getUid(), c.getImportance()));
		}
		final Set<TypeFilterDO> tSet = filter.getFilterTypes();
		for (final TypeFilter t : message.getTypeFilter()) {
			tSet.add(new TypeFilterDO(t.getUid(), t.getImportance(), t
					.isFiltered()));
		}
		return filter;
	}

	public static ScanFilter convert(ScanFilterDO data, String server) {
		final ScanFilter filter = new ScanFilter();
		filter.setName(data.getName());
		filter.setUid(data.getUid());
		filter.setRevision(data.getRevision());
		filter.setOwner(server);
		final List<CategoryFilter> cSet = filter.getCategoryFilter();
		for (final CategoryFilterDO c : data.getCategories()) {
			final CategoryFilter cf = new CategoryFilter();
			cf.setUid(c.getUid());
			c.setImportance(cf.getImportance());
			cSet.add(cf);
		}
		final List<TypeFilter> tSet = filter.getTypeFilter();
		for (final TypeFilterDO t : data.getFilterTypes()) {
			final TypeFilter tf = new TypeFilter();
			tf.setUid(t.getFindingType());
			tf.setImportance(t.getImportance());
			tf.setFiltered(t.isFiltered());
			tSet.add(tf);
		}
		return filter;
	}

}
