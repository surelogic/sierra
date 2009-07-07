package com.surelogic.sierra.jdbc.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.surelogic.common.jdbc.LongIdHandler;
import com.surelogic.common.jdbc.NullRowHandler;
import com.surelogic.common.jdbc.Nulls;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Queryable;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.common.jdbc.SingleRowHandler;
import com.surelogic.common.jdbc.StringRowHandler;
import com.surelogic.sierra.jdbc.settings.Categories;
import com.surelogic.sierra.jdbc.settings.CategoryDO;
import com.surelogic.sierra.jdbc.settings.CategoryEntryDO;
import com.surelogic.sierra.jdbc.settings.CategoryFilterDO;
import com.surelogic.sierra.jdbc.settings.ScanFilterDO;
import com.surelogic.sierra.jdbc.settings.TypeFilterDO;
import com.surelogic.sierra.tool.message.CategoryFilter;
import com.surelogic.sierra.tool.message.Extension;
import com.surelogic.sierra.tool.message.ExtensionArtifactType;
import com.surelogic.sierra.tool.message.ExtensionFindingType;
import com.surelogic.sierra.tool.message.ScanFilter;
import com.surelogic.sierra.tool.message.TypeFilter;

public class FindingTypes {

	private final Query q;

	public FindingTypes(final Query q) {
		this.q = q;
	}

	/**
	 * Lists all of the finding types in the database.
	 * 
	 * @return
	 */
	public List<FindingTypeDO> listFindingTypes() {
		return getFindingTypes(q.statement("FindingTypes.listFindingTypes",
				new StringRowHandler()).call());
	}

	/**
	 * Gets the list of finding types corresponding to the given uids.
	 * 
	 * @param uids
	 * @return an in-order list of {@link FindingTypeDO} objects
	 */
	public List<FindingTypeDO> getFindingTypes(final Collection<String> uids) {
		final List<FindingTypeDO> list = new ArrayList<FindingTypeDO>();
		final Queryable<FindingTypeDO> getType = q.prepared(
				"FindingTypes.findByUid", new FindingTypeDOHandler());
		final Queryable<List<ArtifactTypeDO>> getArts = q.prepared(
				"FindingTypes.findArtifactTypeById",
				new ArtifactTypeDOHandler());
		for (final String uid : uids) {
			if (uid == null) {
				throw new IllegalArgumentException("May not be null");
			}
			final FindingTypeDO t = getType.call(uid);
			t.getArtifactTypes().addAll(getArts.call(t.getId()));
			list.add(t);
		}
		return list;
	}

	/**
	 * Get the finding type corresponding the the given uid
	 * 
	 * @param uid
	 * @return
	 */
	public FindingTypeDO getFindingType(final String uid) {
		if (uid == null) {
			throw new IllegalArgumentException("May not be null");
		}
		final FindingTypeDO t = q.prepared("FindingTypes.findByUid",
				new FindingTypeDOHandler()).call(uid);
		t.getArtifactTypes().addAll(
				q.prepared("FindingTypes.findArtifactTypeById",
						new ArtifactTypeDOHandler()).call(t.getId()));
		return t;
	}

	public FindingTypeDO getFindingType(final long id) {
		return q.prepared("FindingTypes.findById", new FindingTypeDOHandler())
				.call(id);
	}

	/**
	 * Get the list of artifact types belonging to a specific tool
	 * 
	 * @param tool
	 *            The name of the tool.
	 * @param version
	 *            the version of the tool. May not be null
	 * @return
	 */
	public List<ArtifactTypeDO> getToolArtifactTypes(final String tool,
			final String version) {
		return q.prepared("FindingTypes.artifactsByTool",
				new ArtifactTypeDOHandler()).call(tool, version);
	}

	/**
	 * Register an extension in the database. This will create all artifact
	 * types and finding types in the database, making them available for scan
	 * generation.
	 * 
	 * <p>
	 * <b>NOTE:</b> {@link ExtensionDO} makes use of {@link FindingTypeDO}, but
	 * when registering an extension the list of {@link ArtifactTypeDO} objects
	 * referred to by {@link FindingTypeDO#getArtifactTypes()} is ignored.
	 * </p>
	 * 
	 * @param e
	 */
	public void registerExtension(final ExtensionDO e) {
		if (e.getName() == null || e.getVersion() == null
				|| e.getName().length() == 0 || e.getVersion().length() == 0) {
			throw new IllegalArgumentException();
		}
		final long id = q.prepared("FindingTypes.registerExtension",
				new LongIdHandler()).call(e.getName(), e.getVersion());
		final Queryable<Long> insertFT = q.prepared(
				"FindingTypes.insertFindingType", new LongIdHandler());
		final Queryable<?> registerFT = q
				.prepared("FindingTypes.registerExtensionFindingType");
		final List<String> allFindingTypes = new ArrayList<String>();
		for (final FindingTypeDO ft : e.getNewFindingTypes()) {
			final long ftId = insertFT.call(ft.getUid(), ft.getName(), ft
					.getShortMessage(), ft.getInfo());
			registerFT.call(id, ftId);
			allFindingTypes.add(ft.getUid());
		}

		allFindingTypes.addAll(e.getArtifactMap().keySet());
		final CategoryDO cDO = new CategoryDO();
		cDO.setName(e.getName());
		cDO.setUid(UUID.randomUUID().toString());
		for (final String ft : allFindingTypes) {
			cDO.getFilters().add(new CategoryEntryDO(ft, false));
		}
		// FIXME this is not how we make categories
		new Categories(q).writeCategory(cDO);

		final Queryable<Long> insertAT = q.prepared(
				"FindingTypes.insertArtifactType", new LongIdHandler());
		final Queryable<?> registerAT = q
				.prepared("FindingTypes.registerExtensionArtifact");
		for (final Entry<String, List<ArtifactTypeDO>> entry : e
				.getArtifactMap().entrySet()) {
			for (final ArtifactTypeDO art : entry.getValue()) {
				final long artId = insertAT.call(art.getTool(), art
						.getVersion(), art.getMnemonic(), art.getDisplay(),
						Nulls.STRING, Nulls.STRING, Nulls.STRING, entry
								.getKey());
				registerAT.call(id, artId);
			}
		}
	}

	/**
	 * Return a list of all of the available extensions registered in the
	 * database.
	 * 
	 * @return
	 */
	public List<ExtensionDO> getExtensions() {
		return q.prepared("FindingTypes.selectExtensions",
				new RowHandler<ExtensionDO>() {
					public ExtensionDO handle(final Row r) {
						final ExtensionDO e = new ExtensionDO();
						final long id = r.nextLong();
						e.setName(r.nextString());
						e.setVersion(r.nextString());
						q.prepared("FindingTypes.selectExtensionArtifactTypes",
								new NullRowHandler() {
									ArtifactTypeDOHandler h = new ArtifactTypeDOHandler();

									@Override
									protected void doHandle(final Row r) {
										e.addType(r.nextString(), h.handle(r));
									}
								}).call(id);
						for (final FindingTypeDO ft : getFindingTypes(q
								.prepared(
										"FindingTypes.selectExtensionFindingTypes",
										new StringRowHandler()).call(id))) {
							e.addFindingType(ft);
						}
						return e;
					}
				}).call();

	}

	private static class FindingTypeDOHandler extends
			SingleRowHandler<FindingTypeDO> {
		@Override
		public FindingTypeDO handleRow(final Row r) {
			final FindingTypeDO ft = new FindingTypeDO();
			ft.setId(r.nextLong());
			ft.setUid(r.nextString());
			ft.setName(r.nextString());
			ft.setShortMessage(r.nextString());
			ft.setInfo(r.nextString());
			return ft;
		}
	}

	private static class ArtifactTypeDOHandler implements
			RowHandler<ArtifactTypeDO> {
		public ArtifactTypeDO handle(final Row r) {
			return new ArtifactTypeDO(r.nextLong(), r.nextString(), r
					.nextString(), r.nextString(), r.nextString());
		}

	}

	public static ExtensionDO convertDO(final Extension message) {
		final ExtensionDO e = new ExtensionDO(message.getName(), message
				.getVersion());
		for (final ExtensionArtifactType a : message.getArtifacts()) {
			e.addType(a.getFindingType(), new ArtifactTypeDO(a.getTool(), a
					.getMnemonic(), a.getDisplay(), a.getVersion()));
		}
		for (final ExtensionFindingType ft : message.getFindingTypes()) {
			e.addFindingType(convertDO(ft));
		}
		for (final ExtensionArtifactType at : message.getArtifacts()) {
			e.addType(at.getFindingType(), new ArtifactTypeDO(at.getTool(), at
					.getMnemonic(), at.getDisplay(), at.getVersion()));
		}
		return e;
	}

	public static FindingTypeDO convertDO(final ExtensionFindingType eft) {
		final FindingTypeDO ft = new FindingTypeDO();
		ft.setUid(eft.getId());
		ft.setInfo(eft.getInfo());
		ft.setName(eft.getName());
		ft.setShortMessage(eft.getShortMessage());
		return ft;
	}

	public static Extension convert(final ExtensionDO data) {
		final Extension e = new Extension();
		e.setName(data.getName());
		e.setVersion(data.getVersion());
		for (final FindingTypeDO ftDO : data.getNewFindingTypes()) {
			final ExtensionFindingType ft = new ExtensionFindingType();
			ft.setId(ftDO.getUid());
			ft.setInfo(ftDO.getInfo());
			ft.setName(ftDO.getName());
			ft.setShortMessage(ftDO.getShortMessage());
			e.getFindingTypes().add(ft);
		}
		for (final Entry<String, List<ArtifactTypeDO>> entry : data
				.getArtifactMap().entrySet()) {
			for (final ArtifactTypeDO atDO : entry.getValue()) {
				final ExtensionArtifactType at = new ExtensionArtifactType();
				at.setDisplay(atDO.getDisplay());
				at.setFindingType(entry.getKey());
				at.setMnemonic(atDO.getMnemonic());
				at.setTool(atDO.getTool());
				at.setVersion(atDO.getVersion());
				e.getArtifacts().add(at);
			}
		}
		return e;
	}

	public static ScanFilterDO convertDO(final ScanFilter message) {
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

	public static ScanFilter convert(final ScanFilterDO data, final String owner) {
		final ScanFilter filter = new ScanFilter();
		filter.setName(data.getName());
		filter.setUid(data.getUid());
		filter.setRevision(data.getRevision());
		filter.setOwner(owner);
		final List<CategoryFilter> cSet = filter.getCategoryFilter();
		for (final CategoryFilterDO c : data.getCategories()) {
			final CategoryFilter cf = new CategoryFilter();
			cf.setUid(c.getUid());
			cf.setImportance(c.getImportance());
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
