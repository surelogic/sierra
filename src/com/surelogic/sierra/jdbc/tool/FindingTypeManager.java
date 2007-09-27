package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.Settings;

public class FindingTypeManager {

	private static final Logger log = SLLogger
			.getLoggerFor(FindingTypeManager.class);

	private final PreparedStatement checkForArtifactTypeRelation;
	private final PreparedStatement selectArtifactTypesByFindingType;
	private final PreparedStatement selectArtifactType;
	private final PreparedStatement selectArtifactTypesByToolAndMnemonic;
	private final PreparedStatement insertArtifactTypeFindingTypeRelation;
	private final PreparedStatement insertCategoryFindingTypeRelation;
	private final PreparedStatement checkUnassignedArtifactTypes;
	private final PreparedStatement checkUncategorizedFindingTypes;
	private final FindingTypeRecordFactory factory;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.checkForArtifactTypeRelation = conn
				.prepareStatement("SELECT FT.UID FROM ARTIFACT_TYPE_FINDING_TYPE_RELTN ATFTR, FINDING_TYPE FT WHERE ATFTR.ARTIFACT_TYPE_ID = ? AND FT.ID = ATFTR.FINDING_TYPE_ID");
		this.selectArtifactType = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND T.VERSION = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.selectArtifactTypesByFindingType = conn
				.prepareStatement("SELECT AR.ID FROM FINDING_TYPE FT, ARTIFACT_TYPE_FINDING_TYPE_RELTN AFTR, ARTIFACT_TYPE AR WHERE FT.NAME = ? AND AFTR.FINDING_TYPE_ID = FT.ID AND AR.ID = AFTR.ARTIFACT_TYPE_ID");
		this.selectArtifactTypesByToolAndMnemonic = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.insertArtifactTypeFindingTypeRelation = conn
				.prepareStatement("INSERT INTO ARTIFACT_TYPE_FINDING_TYPE_RELTN (ARTIFACT_TYPE_ID,FINDING_TYPE_ID) VALUES (?,?)");
		this.insertCategoryFindingTypeRelation = conn
				.prepareStatement("INSERT INTO CATEGORY_FINDING_TYPE_RELTN (CATEGORY_ID,FINDING_TYPE_ID) VALUES (?,?)");
		this.checkUnassignedArtifactTypes = conn
				.prepareStatement("SELECT T.NAME,T.VERSION,A.MNEMONIC FROM ARTIFACT_TYPE A, TOOL T WHERE A.ID IN ((SELECT ID FROM ARTIFACT_TYPE) EXCEPT (SELECT ARTIFACT_TYPE_ID FROM ARTIFACT_TYPE_FINDING_TYPE_RELTN)) AND T.ID = A.TOOL_ID");
		this.checkUncategorizedFindingTypes = conn
				.prepareStatement("SELECT UID,NAME FROM FINDING_TYPE WHERE ID IN ((SELECT ID FROM FINDING_TYPE) EXCEPT (SELECT FINDING_TYPE_ID FROM CATEGORY_FINDING_TYPE_RELTN))");
		this.factory = FindingTypeRecordFactory.getInstance(conn);
	}

	public Long getFindingTypeId(String name) throws SQLException {
		FindingTypeRecord ft = factory.newFindingTypeRecord();
		ft.setName(name);
		if (ft.select()) {
			return ft.getId();
		}
		return null;
	}

	public Long getArtifactTypeId(String tool, String version, String mnemonic)
			throws SQLException {
		selectArtifactType.setString(1, tool);
		selectArtifactType.setString(2, version);
		selectArtifactType.setString(3, mnemonic);
		ResultSet set = selectArtifactType.executeQuery();
		if (set.next()) {
			return set.getLong(1);
		} else {
			return null;
		}
	}

	private Collection<Long> getArtifactTypes(String tool, String mnemonic)
			throws SQLException {
		List<Long> ids = new LinkedList<Long>();
		selectArtifactTypesByToolAndMnemonic.setString(1, tool);
		selectArtifactTypesByToolAndMnemonic.setString(2, mnemonic);
		ResultSet set = selectArtifactTypesByToolAndMnemonic.executeQuery();
		while (set.next()) {
			ids.add(set.getLong(1));
		}
		return ids;
	}

	public MessageFilter getMessageFilter(Settings settings)
			throws SQLException {
		if (settings != null) {
			Collection<FindingTypeFilter> filters = settings.getFilter();
			Map<Long, FindingTypeFilter> findingMap = new HashMap<Long, FindingTypeFilter>(
					filters.size());
			Map<Long, FindingTypeFilter> artifactMap = new HashMap<Long, FindingTypeFilter>(
					filters.size() * 2);
			FindingTypeRecord ft = factory.newFindingTypeRecord();
			for (FindingTypeFilter filter : filters) {
				ft.setName(filter.getName());
				if (ft.select()) {
					findingMap.put(ft.getId(), filter);
				}
				selectArtifactTypesByFindingType.setString(1, filter.getName());
				ResultSet set = selectArtifactTypesByFindingType.executeQuery();
				while (set.next()) {
					artifactMap.put(set.getLong(1), filter);
				}
			}
			return new MessageFilter(findingMap, artifactMap);
		} else {
			Map<Long, FindingTypeFilter> map = Collections.emptyMap();
			return new MessageFilter(map, map);
		}
	}

	public void updateFindingTypes(List<FindingTypes> types)
			throws SQLException {
		for (FindingTypes type : types) {
			FindingTypeRecord fRec = factory.newFindingTypeRecord();
			// Load in all of the finding types, and associate them with
			// artifact
			// types.
			for (FindingType ft : type.getFindingType()) {
				fRec.setUid(ft.getId().trim());
				fRec.setName(ft.getName().trim());
				fRec.setInfo(ft.getInfo());
				fRec.setShortMessage(ft.getShortMessage());
				if (fRec.select()) {
					fRec.update();
				} else {
					fRec.insert();
				}
				for (ArtifactType at : ft.getArtifact()) {
					Collection<Long> typeIds;
					if (at.getVersion() != null) {
						typeIds = getArtifactTypes(at.getTool(), at
								.getMnemonic());
					} else {
						typeIds = Collections.singleton(getArtifactTypeId(at
								.getTool(), at.getVersion(), at.getMnemonic()));
					}
					for (Long id : typeIds) {
						checkForArtifactTypeRelation.setLong(1, id);
						ResultSet set = checkForArtifactTypeRelation
								.executeQuery();
						if (set.next()) {
							String message = "The artifact with mnemonic "
									+ at.getMnemonic()
									+ " in tool "
									+ at.getTool()
									+ " has already been assigned to finding type with uid "
									+ set.getString(1)
									+ " and cannot be assigned to the finding type with uid "
									+ fRec.getUid() + ".";
							log.severe(message);
							throw new IllegalStateException(message);
						}
						insertArtifactTypeFindingTypeRelation.setLong(1, id);
						insertArtifactTypeFindingTypeRelation.setLong(2, fRec
								.getId());
						insertArtifactTypeFindingTypeRelation.executeUpdate();
					}
				}
			}
			CategoryRecord cRec = factory.newCategoryRecord();
			// Load in all of the categories, and associate them with finding
			// types
			for (Category cat : type.getCategory()) {
				cRec.setUid(cat.getId().trim());
				cRec.setName(cat.getName().trim());
				cRec.setDescription(cat.getDescription());
				if (cRec.select()) {
					cRec.update();
				} else {
					cRec.insert();
				}
				for (String ftId : cat.getFindingType()) {
					fRec.setUid(ftId.trim());
					if (fRec.select()) {
						insertCategoryFindingTypeRelation.setLong(1, cRec
								.getId());
						insertCategoryFindingTypeRelation.setLong(2, fRec
								.getId());
						insertCategoryFindingTypeRelation.execute();
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
						.append("The following finding types do nat have an assigned category:\n");
				for (String s : uncategorizedFindings) {
					builder.append(s);
				}
			}
			String message = builder.toString();
			log.severe(message);
			throw new IllegalStateException(message);
		}
	}

	public static FindingTypeManager getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}

}
