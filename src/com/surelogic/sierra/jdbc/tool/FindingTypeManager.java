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

import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.xml.Entities;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.FindingTypes;

public final class FindingTypeManager {

	private static final Logger log = SLLogger
			.getLoggerFor(FindingTypeManager.class);

	private final PreparedStatement selectArtifactTypesByFindingTypeId;
	private final PreparedStatement selectArtifactType;
	private final PreparedStatement selectArtifactTypesByToolAndMnemonic;
	private final PreparedStatement updateArtifactTypeFindingTypeRelation;
	private final PreparedStatement checkUnassignedArtifactTypes;
	private final FindingTypeRecordFactory factory;

	private FindingTypeManager(final Connection conn) throws SQLException {
		selectArtifactType = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND T.VERSION = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		selectArtifactTypesByFindingTypeId = conn
				.prepareStatement("SELECT AR.ID FROM ARTIFACT_TYPE AR WHERE AR.FINDING_TYPE_ID = ?");
		selectArtifactTypesByToolAndMnemonic = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		updateArtifactTypeFindingTypeRelation = conn
				.prepareStatement("UPDATE ARTIFACT_TYPE SET FINDING_TYPE_ID = ? WHERE ID = ?");
		checkUnassignedArtifactTypes = conn
				.prepareStatement("SELECT T.NAME,T.VERSION,A.MNEMONIC FROM ARTIFACT_TYPE A, TOOL T WHERE A.FINDING_TYPE_ID IS NULL AND T.ID = A.TOOL_ID");
		factory = FindingTypeRecordFactory.getInstance(conn);
	}

	/**
	 * Look up a local finding type id by its global identifier.
	 * 
	 * @param uid
	 * @return the local finding type id, or <code>null</code> if none exists
	 * @throws SQLException
	 */
	public Long getFindingTypeId(final String uid) throws SQLException {
		final FindingTypeRecord ft = factory.newFindingTypeRecord();
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
	 * @returnthe the relevant {@link FindingType}, or <code>null</code> if none
	 *            exists
	 * @throws SQLException
	 */
	// TODO Return the artifact types that match this finding type
	public FindingType getFindingType(final String uid) throws SQLException {
		final FindingTypeRecord ft = factory.newFindingTypeRecord();
		ft.setUid(uid);
		if (ft.select()) {
			final FindingType type = new FindingType();
			type.setId(uid);
			type.setInfo(ft.getInfo());
			type.setName(ft.getName());
			type.setShortMessage(ft.getShortMessage());
			return type;
		}
		return null;
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
	public Long getArtifactTypeId(final String tool, final String version,
			final String mnemonic) throws SQLException {
		selectArtifactType.setString(1, tool);
		selectArtifactType.setString(2, version);
		selectArtifactType.setString(3, mnemonic);
		final ResultSet set = selectArtifactType.executeQuery();
		try {
			if (set.next()) {
				final Long id = set.getLong(1);
				return id;
			} else {
				final String message = "No Artifact could be found with tool name "
						+ tool
						+ ", mnemonic "
						+ mnemonic
						+ ", and version "
						+ version + ".";
				log.severe(message);
				throw new IllegalArgumentException(message);
			}
		} finally {
			set.close();
		}
	}

	private Collection<Long> getArtifactTypesIds(final String tool,
			final String mnemonic) throws SQLException {
		final List<Long> ids = new LinkedList<Long>();
		selectArtifactTypesByToolAndMnemonic.setString(1, tool);
		selectArtifactTypesByToolAndMnemonic.setString(2, mnemonic);
		final ResultSet set = selectArtifactTypesByToolAndMnemonic
				.executeQuery();
		try {
			while (set.next()) {
				ids.add(set.getLong(1));
			}
		} finally {
			set.close();
		}
		return ids;
	}

	public FindingFilter getMessageFilter(
			final Collection<FindingTypeFilter> filters) throws SQLException {
		final Map<Long, FindingTypeFilter> findingMap = new HashMap<Long, FindingTypeFilter>(
				filters.size());
		final Map<Long, FindingTypeFilter> artifactMap = new HashMap<Long, FindingTypeFilter>(
				filters.size() * 2);
		final FindingTypeRecord ft = factory.newFindingTypeRecord();
		for (final FindingTypeFilter filter : filters) {
			ft.setUid(filter.getName());
			if (ft.select()) {
				findingMap.put(ft.getId(), filter);
			}
			selectArtifactTypesByFindingTypeId.setLong(1, ft.getId());
			final ResultSet set = selectArtifactTypesByFindingTypeId
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
	}

	/**
	 * Update the local finding type reference information to reflect the
	 * provided {@link FindingTypes} information.
	 * 
	 * @param types
	 * @throws SQLException
	 */
	public void updateFindingTypes(final List<FindingTypes> types,
			final long revision) throws SQLException {
		final Set<String> idSet = new HashSet<String>();
		final Set<String> duplicates = new HashSet<String>();
		final Set<String> categoryDuplicates = new HashSet<String>();
		final Set<Long> artifactIdSet = new HashSet<Long>();
		final Set<String> artifactDuplicates = new HashSet<String>();
		final Set<String> invalidArtifacts = new HashSet<String>();
		final Set<String> invalidTypes = new HashSet<String>();
		for (final FindingTypes type : types) {
			final FindingTypeRecord fRec = factory.newFindingTypeRecord();
			// Load in all of the finding types, and associate them with
			// artifact
			// types.
			for (final FindingType ft : type.getFindingType()) {
				if (ft.getId() == null || ft.getId().length() == 0
						|| ft.getName() == null || ft.getName().length() == 0) {
					invalidTypes.add("Id: " + ft.getId() + " Name: "
							+ ft.getName());
					continue;
				}
				final String uid = ft.getId().trim();
				if (!idSet.add(uid)) {
					duplicates.add(uid);
				} else {
					fRec.setUid(uid);
					final boolean exists = fRec.select();
					fRec.setName(ft.getName().trim());
					fRec.setInfo(ft.getInfo().trim());
					fRec.setShortMessage(Entities.trimInternal(ft
							.getShortMessage().trim()));
					if (exists) {
						fRec.update();
					} else {
						fRec.insert();
					}
					final List<ArtifactType> artifactTypes = ft.getArtifact();
					if (artifactTypes != null && !artifactTypes.isEmpty()) {
						for (final ArtifactType at : artifactTypes) {
							Collection<Long> typeIds;
							if (at.getVersion() == null) {
								typeIds = getArtifactTypesIds(at.getTool(),
										at.getMnemonic());
							} else {
								typeIds = Collections
										.singleton(getArtifactTypeId(
												at.getTool(), at.getVersion(),
												at.getMnemonic()));
							}
							if (typeIds.isEmpty()) {
								invalidArtifacts.add(at.getMnemonic() + " in "
										+ at.getTool());
							}
							for (final long id : typeIds) {
								if (!artifactIdSet.add(id)) {
									artifactDuplicates.add(at.getTool() + ":"
											+ at.getMnemonic());
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
		}
		// Ensure that all artifact types belong to a finding type, and all
		// finding types belong to a category
		final ArrayList<String> unassignedArtifacts = new ArrayList<String>();
		final ArrayList<String> uncategorizedFindings = new ArrayList<String>();
		final ResultSet set = checkUnassignedArtifactTypes.executeQuery();
		try {
			while (set.next()) {
				unassignedArtifacts.add("\tTool: " + set.getString(1)
						+ " Version: " + set.getString(2) + " Mnemonic: "
						+ set.getString(3) + "\n");
			}
		} finally {
			set.close();
		}

		if (!invalidTypes.isEmpty()) {
			final String message = "The following finding types have an invalid name or id: "
					+ invalidTypes;
			log.severe(message);
			throw new IllegalStateException(message);
		}
		if (!invalidArtifacts.isEmpty()) {
			final String message = "The following artifact types do not actually exist in the system: "
					+ invalidArtifacts + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// FIXME We want to check to see if a finding type doesn't belong to any
		// filter set
		if (!unassignedArtifacts.isEmpty() || !uncategorizedFindings.isEmpty()) {
			final StringBuilder builder = new StringBuilder();
			if (!unassignedArtifacts.isEmpty()) {
				builder.append("The following artifact types do not have an assigned finding type:\n");
				for (final String s : unassignedArtifacts) {
					builder.append(s);
				}
			}
			if (!uncategorizedFindings.isEmpty()) {
				builder.append("The following finding types do not have an assigned category:\n");
				for (final String s : uncategorizedFindings) {
					builder.append(s);
				}
			}
			final String message = builder.toString();
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if any finding type is defined more than once
		if (!duplicates.isEmpty()) {
			final String message = "The finding types with the following ids were defined more than once: "
					+ duplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if any finding type belongs to more than one category
		if (!categoryDuplicates.isEmpty()) {
			final String message = "The finding types with the following ids were defined more than once: "
					+ categoryDuplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
		// Throw exception if we tried to assign an artifact type to multiple
		// finding types
		if (!artifactDuplicates.isEmpty()) {
			final String message = "The artifact types with the following ids were assigned to finding types more than once: "
					+ artifactDuplicates + ".";
			log.severe(message);
			throw new IllegalStateException(message);
		}
	}

	public static FindingTypeManager getInstance(final Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}

}
