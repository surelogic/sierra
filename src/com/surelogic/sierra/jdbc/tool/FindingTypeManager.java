package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.surelogic.common.Entities;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.settings.CategoryView;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Settings;
import com.surelogic.sierra.tool.message.Severity;

public class FindingTypeManager {

	private static final Logger log = SLLogger
			.getLoggerFor(FindingTypeManager.class);

	private static final FindingFilter EMPTY_FILTER = new FindingFilter() {

		public boolean accept(Long artifactTypeId) {
			return true;
		}

		public Importance calculateImportance(Long findingTypeId,
				Priority priority, Severity severity) {
			int val = ((int) (((float) (severity.ordinal() + priority.ordinal())) / 2));
			if (val > 3) {
				val = 3;
			} else if (val < 1) {
				val = 1;
			}
			return Importance.values()[val];
		}
	};

	private final PreparedStatement listCategories;
	private final PreparedStatement listCategoryFindingTypes;
	private final PreparedStatement selectArtifactTypesByFindingTypeId;
	private final PreparedStatement selectArtifactType;
	private final PreparedStatement selectArtifactTypesByToolAndMnemonic;
	private final PreparedStatement updateArtifactTypeFindingTypeRelation;
	private final PreparedStatement updateCategoryFindingTypeRelation;
	private final PreparedStatement checkUnassignedArtifactTypes;
	private final PreparedStatement checkUncategorizedFindingTypes;
	private final FindingTypeRecordFactory factory;
	private final Connection conn;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.conn = conn;
		this.selectArtifactType = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND T.VERSION = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.selectArtifactTypesByFindingTypeId = conn
				.prepareStatement("SELECT AR.ID FROM ARTIFACT_TYPE AR WHERE AR.FINDING_TYPE_ID = ?");
		this.selectArtifactTypesByToolAndMnemonic = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.updateArtifactTypeFindingTypeRelation = conn
				.prepareStatement("UPDATE ARTIFACT_TYPE SET FINDING_TYPE_ID = ? WHERE ID = ?");
		this.updateCategoryFindingTypeRelation = conn
				.prepareStatement("UPDATE FINDING_TYPE SET CATEGORY_ID = ? WHERE ID = ?");
		this.checkUnassignedArtifactTypes = conn
				.prepareStatement("SELECT T.NAME,T.VERSION,A.MNEMONIC FROM ARTIFACT_TYPE A, TOOL T WHERE A.FINDING_TYPE_ID IS NULL AND T.ID = A.TOOL_ID");
		this.checkUncategorizedFindingTypes = conn
				.prepareStatement("SELECT UUID,NAME FROM FINDING_TYPE WHERE CATEGORY_ID IS NULL");
		listCategories = conn
				.prepareStatement("SELECT UUID,NAME FROM FINDING_CATEGORY");
		listCategoryFindingTypes = conn
				.prepareStatement("SELECT UUID FROM FINDING_TYPE WHERE CATEGORY_ID = ?");
		this.factory = FindingTypeRecordFactory.getInstance(conn);
	}

	/**
	 * Look up a local finding type id by its global identifier.
	 * 
	 * @param uid
	 * @return the local finding type id, or <code>null</code> if none exists
	 * @throws SQLException
	 */
	public Long getFindingTypeId(String uid) throws SQLException {
		FindingTypeRecord ft = factory.newFindingTypeRecord();
		ft.setUid(uid);
		if (ft.select()) {
			return ft.getId();
		}
		return null;
	}

	/**
	 * Look up finding type information by its global identifier.
	 * 
	 * @param uid
	 * @returnthe the relevant {@link FindingType}, or <code>null</code> if
	 *            none exists
	 * @throws SQLException
	 */
	// TODO Return the artifact types that match this finding type
	public FindingType getFindingType(String uid) throws SQLException {
		FindingTypeRecord ft = factory.newFindingTypeRecord();
		ft.setUid(uid);
		if (ft.select()) {
			FindingType type = new FindingType();
			type.setId(uid);
			type.setInfo(ft.getInfo());
			type.setName(ft.getName());
			type.setShortMessage(ft.getShortMessage());
			return type;
		}
		return null;
	}

	/**
	 * Look up a local category id by its global identifier.
	 * 
	 * @param uid
	 * @return the local category id, or <code>null</code> if none exists
	 * @throws SQLException
	 */
	public Long getCategoryId(String uid) throws SQLException {
		CategoryRecord ctRec = factory.newCategoryRecord();
		ctRec.setUid(uid);
		if (ctRec.select()) {
			return ctRec.getId();
		}
		return null;
	}

	/**
	 * Look up category information by its global identifier.
	 * 
	 * @param uid
	 * @return the relevant {@link Category}, or <code>null</code> if none
	 *         exists
	 * @throws SQLException
	 */
	public Category getCategory(String uid) throws SQLException {
		CategoryRecord ctRec = factory.newCategoryRecord();
		ctRec.setUid(uid);
		if (ctRec.select()) {
			Category c = new Category();
			c.setDescription(ctRec.getDescription());
			c.setId(uid);
			c.setName(ctRec.getName());
			listCategoryFindingTypes.setLong(1, ctRec.getId());
			ResultSet set = listCategoryFindingTypes.executeQuery();
			try {
				final List<String> findingTypes = c.getFindingType();
				while (set.next()) {
					findingTypes.add(set.getString(1));
				}
			} finally {
				set.close();
			}
			return c;
		} else {
			return null;
		}
	}

	/**
	 * Look up a local artifact type id.
	 * 
	 * @param tool
	 * @param version
	 * @param mnemonic
	 * @return
	 * @throws SQLException
	 */
	public Long getArtifactTypeId(String tool, String version, String mnemonic)
			throws SQLException {
		selectArtifactType.setString(1, tool);
		selectArtifactType.setString(2, version);
		selectArtifactType.setString(3, mnemonic);
		ResultSet set = selectArtifactType.executeQuery();
		try {
			if (set.next()) {
				Long id = set.getLong(1);
				return id;
			} else {
				String message = "No Artifact could be found with tool name "
						+ tool + ", mnemonic " + mnemonic + ", and version "
						+ version + ".";
				log.severe(message);
				throw new IllegalArgumentException(message);
			}
		} finally {
			set.close();
		}
	}

	private Collection<Long> getArtifactTypesIds(String tool, String mnemonic)
			throws SQLException {
		List<Long> ids = new LinkedList<Long>();
		selectArtifactTypesByToolAndMnemonic.setString(1, tool);
		selectArtifactTypesByToolAndMnemonic.setString(2, mnemonic);
		ResultSet set = selectArtifactTypesByToolAndMnemonic.executeQuery();
		try {
			while (set.next()) {
				ids.add(set.getLong(1));
			}
		} finally {
			set.close();
		}
		if (ids.isEmpty()) {
			String message = "No artifact types could be found with tool "
					+ tool + " and mnemonic " + mnemonic + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		return ids;
	}

	public FindingFilter getMessageFilter(Collection<FindingTypeFilter> filters)
			throws SQLException {
		Map<Long, FindingTypeFilter> findingMap = new HashMap<Long, FindingTypeFilter>(
				filters.size());
		Map<Long, FindingTypeFilter> artifactMap = new HashMap<Long, FindingTypeFilter>(
				filters.size() * 2);
		FindingTypeRecord ft = factory.newFindingTypeRecord();
		for (FindingTypeFilter filter : filters) {
			ft.setUid(filter.getName());
			if (ft.select()) {
				findingMap.put(ft.getId(), filter);
			}
			selectArtifactTypesByFindingTypeId.setLong(1, ft.getId());
			ResultSet set = selectArtifactTypesByFindingTypeId.executeQuery();
			try {
				while (set.next()) {
					artifactMap.put(set.getLong(1), filter);
				}
			} finally {
				set.close();
			}
		}
		return new MessageFilter(findingMap, artifactMap);
	}

	public FindingFilter getMessageFilter(Settings settings)
			throws SQLException {
		if (settings != null) {
			final Collection<FindingTypeFilter> filters = SettingsManager
					.getInstance(conn).getSettingFilters(settings.getUid());
			Map<Long, FindingTypeFilter> findingMap = new HashMap<Long, FindingTypeFilter>(
					filters.size());
			Map<Long, FindingTypeFilter> artifactMap = new HashMap<Long, FindingTypeFilter>(
					filters.size() * 2);
			FindingTypeRecord ft = factory.newFindingTypeRecord();
			for (FindingTypeFilter filter : filters) {
				ft.setUid(filter.getName());
				if (ft.select()) {
					findingMap.put(ft.getId(), filter);
				}
				selectArtifactTypesByFindingTypeId.setLong(1, ft.getId());
				ResultSet set = selectArtifactTypesByFindingTypeId
						.executeQuery();
				try {
					while (set.next()) {
						artifactMap.put(set.getLong(1), filter);
					}
				} finally {
					set.close();
				}
			}
			return new MessageFilter(findingMap, artifactMap);
		} else {
			return EMPTY_FILTER;
		}
	}

	public List<CategoryView> listCategories() throws SQLException {
		ResultSet set = listCategories.executeQuery();
		List<CategoryView> view = new ArrayList<CategoryView>();
		try {
			while (set.next()) {
				view.add(new CategoryView(set.getString(1), set.getString(2)));
			}
		} finally {
			set.close();
		}
		return view;
	}

	/**
	 * Update the local finding type reference information to reflect the
	 * provided {@link FindingTypes} information.
	 * 
	 * @param types
	 * @throws SQLException
	 */
	public void updateFindingTypes(List<FindingTypes> types, long revision)
			throws SQLException {
		Set<String> idSet = new HashSet<String>();
		Set<String> duplicates = new HashSet<String>();
		Set<String> categoryIdSet = new HashSet<String>();
		Set<String> categoryDuplicates = new HashSet<String>();
		Set<Long> artifactIdSet = new HashSet<Long>();
		Set<Long> artifactDuplicates = new HashSet<Long>();
		for (FindingTypes type : types) {
			FindingTypeRecord fRec = factory.newFindingTypeRecord();
			// Load in all of the finding types, and associate them with
			// artifact
			// types.
			for (FindingType ft : type.getFindingType()) {
				String uid = ft.getId().trim();
				if (!idSet.add(uid)) {
					duplicates.add(uid);
				} else {
					fRec.setUid(uid);
					boolean exists = fRec.select();
					fRec.setName(ft.getName().trim());
					fRec.setInfo(ft.getInfo().trim());
					fRec.setShortMessage(Entities.trimInternal(ft
							.getShortMessage().trim()));
					if (exists) {
						fRec.update();
					} else {
						fRec.insert();
					}
					List<ArtifactType> artifactTypes = ft.getArtifact();
					if (artifactTypes != null && !artifactTypes.isEmpty()) {
						for (ArtifactType at : artifactTypes) {
							Collection<Long> typeIds;
							if (at.getVersion() == null) {
								typeIds = getArtifactTypesIds(at.getTool(), at
										.getMnemonic());
							} else {
								typeIds = Collections
										.singleton(getArtifactTypeId(at
												.getTool(), at.getVersion(), at
												.getMnemonic()));
							}
							for (long id : typeIds) {
								if (!artifactIdSet.add(id)) {
									artifactDuplicates.add(id);
								} else {
									updateArtifactTypeFindingTypeRelation
											.setLong(1, fRec.getId());
									updateArtifactTypeFindingTypeRelation
											.setLong(2, id);
									updateArtifactTypeFindingTypeRelation
											.executeUpdate();
								}
							}
						}
					}
				}
			}
			CategoryRecord cRec = factory.newCategoryRecord();
			// Load in all of the categories, and associate them with finding
			// types
			for (Category cat : type.getCategory()) {
				cRec.setUid(cat.getId().trim());
				boolean exists = cRec.select();
				cRec.setName(cat.getName().trim());
				cRec.setDescription(cat.getDescription());
				if (exists) {
					cRec.update();
				} else {
					cRec.insert();
				}
				for (String ftId : cat.getFindingType()) {
					String uid = ftId.trim();
					if (!categoryIdSet.add(uid)) {
						categoryDuplicates.add(uid);
					} else {
						fRec.setUid(uid);
						if (fRec.select()) {
							updateCategoryFindingTypeRelation.setLong(1, cRec
									.getId());
							updateCategoryFindingTypeRelation.setLong(2, fRec
									.getId());
							updateCategoryFindingTypeRelation.execute();
						} else {
							String message = "Could not locate a finding type for record with uid "
									+ ftId
									+ " while building category "
									+ cat.getName() + ".";
							log.severe(message);
							throw new IllegalStateException(message);
						}
					}
				}
			}
			final SettingsManager sMan = SettingsManager.getInstance(conn);
			for (Category cat : type.getCategory()) {
				// TODO in the future, this needs to be an update.
				final String uid = sMan.createFilterSet(cat.getName().trim(),
						cat.getDescription(), revision);
				final FilterSet filterSet = sMan.getFilterSet(uid);
				final List<FilterEntry> entries = filterSet.getFilter();
				for (String findingType : cat.getFindingType()) {
					findingType = findingType.trim();
					FilterEntry entry = new FilterEntry();
					entry.setFiltered(false);
					entry.setType(findingType);
					entries.add(entry);
				}
				sMan.updateFilterSet(filterSet, revision);
			}
		}
		// Ensure that all artifact types belong to a finding type, and all
		// finding types belong to a category
		ArrayList<String> unassignedArtifacts = new ArrayList<String>();
		ArrayList<String> uncategorizedFindings = new ArrayList<String>();
		ResultSet set = checkUnassignedArtifactTypes.executeQuery();
		while (set.next()) {
			unassignedArtifacts.add("\tTool: " + set.getString(1)
					+ " Version: " + set.getString(2) + " Mnemonic: "
					+ set.getString(3) + "\n");
		}
		set = checkUncategorizedFindingTypes.executeQuery();
		while (set.next()) {
			uncategorizedFindings.add("\tId: " + set.getString(1) + " Name: "
					+ set.getString(2) + "\n");
		}
		if (!unassignedArtifacts.isEmpty() || !uncategorizedFindings.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			if (!unassignedArtifacts.isEmpty()) {
				builder
						.append("The following artifact types do not have an assigned finding type:\n");
				for (String s : unassignedArtifacts) {
					builder.append(s);
				}
			}
			if (!uncategorizedFindings.isEmpty()) {
				builder
						.append("The following finding types do not have an assigned category:\n");
				for (String s : uncategorizedFindings) {
					builder.append(s);
				}
			}
			String message = builder.toString();
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if any finding type is defined more than once
		if (!duplicates.isEmpty()) {
			String message = "The finding types with the following ids were defined more than once: "
					+ duplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if any finding type belongs to more than one category
		if (!categoryDuplicates.isEmpty()) {
			String message = "The finding types with the following ids were defined more than once: "
					+ categoryDuplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if we tried to assign an artifact type to multiple
		// finding types
		if (!artifactDuplicates.isEmpty()) {
			String message = "The artifact types with the following ids were assigned to finding types more than once: "
					+ artifactDuplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
	}

	public static FindingTypeManager getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}

}
