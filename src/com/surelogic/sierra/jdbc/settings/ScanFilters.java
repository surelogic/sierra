package com.surelogic.sierra.jdbc.settings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.surelogic.sierra.jdbc.Nulls;
import com.surelogic.sierra.jdbc.Query;
import com.surelogic.sierra.jdbc.Queryable;
import com.surelogic.sierra.jdbc.Row;
import com.surelogic.sierra.jdbc.RowHandler;
import com.surelogic.sierra.jdbc.SingleRowHandler;
import com.surelogic.sierra.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.server.RevisionException;
import com.surelogic.sierra.tool.message.Importance;

public class ScanFilters {

	private final Query q;

	public ScanFilters(Query q) {
		this.q = q;
	}

	public List<ScanFilterDO> listScanFilters() {
		final List<ScanFilterDO> list = new ArrayList<ScanFilterDO>();
		for (final String s : q.prepared("ScanFilters.listScanFilters",
				new StringRowHandler()).call()) {
			list.add(getScanFilter(s));
		}
		return list;
	}

	/**
	 * Create empty settings
	 * 
	 * @param name
	 * @param revision
	 * @return the uid of this filter set
	 * @throws SQLException
	 */
	public ScanFilterDO createScanFilter(String name, long revision) {
		final SettingsRecord settingsRec = q.record(SettingsRecord.class);
		settingsRec.setName(name);
		settingsRec.setRevision(revision);
		settingsRec.setUid(UUID.randomUUID().toString());
		settingsRec.insert();
		return getScanFilter(settingsRec.getUid());
	}

	public ScanFilterDO updateScanFilter(ScanFilterDO settings, long revision) {
		final SettingsRecord settingsRec = q.record(SettingsRecord.class);
		settingsRec.setUid(settings.getUid());
		if (settingsRec.select()) {
			if (settingsRec.getRevision() == settings.getRevision()) {
				settingsRec.setName(settings.getName());
				settingsRec.setRevision(revision);
				settingsRec.update();
				q.prepared("ScanFilters.deleteTypeFilters").call(
						settings.getUid());
				q.prepared("ScanFilters.deleteCategoryFilters").call(
						settings.getUid());
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
					insertCategoryFilter.call(cat.getUid(),
							cat.getImportance() == null ? Nulls.STRING : cat
									.getImportance(), cat.isFiltered());
				}
				for (final TypeFilterDO type : settings.getFilterTypes()) {
					insertTypeFilter.call(type.getFindingType(), type
							.getImportance() == null ? Nulls.INT : type
							.getImportance().ordinal(), type.isFiltered());
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

	public void writeScanFilter(ScanFilterDO settings) {
		final SettingsRecord settingsRec = q.record(SettingsRecord.class);
		settingsRec.setUid(settings.getUid());
		if (settingsRec.select()) {
			settingsRec.setName(settings.getName());
			settingsRec.setRevision(settings.getRevision());
		} else {
			settingsRec.setUid(settings.getUid());
			settingsRec.setName(settings.getName());
			settingsRec.setRevision(settings.getRevision());
			settingsRec.insert();
		}
		q.prepared("ScanFilters.deleteTypeFilters").call(settings.getUid());
		q.prepared("ScanFilters.deleteCategoryFilters").call(settings.getUid());
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
			insertCategoryFilter.call(cat.getUid(),
					cat.getImportance() == null ? Nulls.STRING : cat
							.getImportance(), cat.isFiltered());
		}
		for (final TypeFilterDO type : settings.getFilterTypes()) {
			insertTypeFilter.call(type.getFindingType(),
					type.getImportance() == null ? Nulls.INT : type
							.getImportance().ordinal(), type.isFiltered());
		}
	}

	public ScanFilterDO getScanFilter(String uid) {
		final SettingsRecord settingsRec = q.record(SettingsRecord.class);
		settingsRec.setUid(uid);
		if (settingsRec.select()) {
			final ScanFilterDO settings = new ScanFilterDO();
			settings.setUid(settingsRec.getUid());
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

	public ScanFilterDO getScanFilterByProject(String project) {
		if (project == null) {
			throw new IllegalArgumentException("Project may not be null.");
		}
		final String uid = q.prepared("ScanFilters.selectByProject",
				SingleRowHandler.fromRowHandler(new StringRowHandler())).call(
				project);
		return uid == null ? null : getScanFilter(uid);
	}

	private static class FilterSetHandler implements
			RowHandler<CategoryFilterDO> {

		public CategoryFilterDO handle(Row r) {
			return new CategoryFilterDO(r.nextString(), toImportance(r
					.nextString()), r.nextBoolean());
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
}
