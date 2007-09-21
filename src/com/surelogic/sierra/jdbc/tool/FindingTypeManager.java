package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.record.CategoryRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.Settings;

public class FindingTypeManager {

	private static final Logger log = SLLogger
			.getLoggerFor(FindingTypeManager.class);

	private final PreparedStatement selectArtifactTypesByFindingType;
	private final PreparedStatement selectArtifactType;
	private final PreparedStatement deleteArtifactTypeRelations;
	private final PreparedStatement selectArtifactTypesByToolAndMnemonic;
	private final PreparedStatement insertArtifactTypeFindingTypeRelation;
	private final UpdateBaseMapper findingTypeMapper;
	private final UpdateBaseMapper categoryMapper;
	private final PreparedStatement deleteCategoriesRelations;
	private final PreparedStatement insertCategoryFindingTypeRelation;
	private final PreparedStatement checkUnassignedArtifactTypes;
	private final PreparedStatement checkUncategorizedFindingTypes;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.selectArtifactType = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND T.VERSION = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.selectArtifactTypesByFindingType = conn
				.prepareStatement("SELECT AR.ID FROM FINDING_TYPE FT, ARTIFACT_TYPE_FINDING_TYPE_RELTN AFTR, ARTIFACT_TYPE AR WHERE FT.NAME = ? AND AFTR.FINDING_TYPE_ID = FT.ID AND AR.ID = AFTR.ARTIFACT_TYPE_ID");
		this.selectArtifactTypesByToolAndMnemonic = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.deleteArtifactTypeRelations = conn
				.prepareStatement("DELETE FROM ARTIFACT_TYPE_FINDING_TYPE_RELTN");
		this.insertArtifactTypeFindingTypeRelation = conn
				.prepareStatement("INSERT INTO ARTIFACT_TYPE_FINDING_TYPE_RELTN (ARTIFACT_TYPE_ID,FINDING_TYPE_ID) VALUES (?,?)");
		this.findingTypeMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_TYPE (UID,NAME,SHORT_MESSAGE,INFO) VALUES (?,?,?,?)",
				"SELECT ID,NAME,SHORT_MESSAGE,INFO FROM FINDING_TYPE WHERE UID = ?",
				"DELETE FROM FINDING_TYPE WHERE ID = ?",
				"UPDATE FINDING_TYPE SET NAME = ?, SHORT_MESSAGE = ?, INFO = ? WHERE ID = ?");
		this.categoryMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_CATEGORY (UID,NAME,DESCRIPTION) VALUES (?,?,?)",
				"SELECT ID,NAME,DESCRIPTION FROM FINDING_CATEGORY WHERE UID = ?",
				"DELETE FROM FINDING_CATEGORY WHERE ID = ?",
				"UPDATE FINDING_CATEGORY SET NAME = ?, DESCRIPTION = ? WHERE ID = ?");
		this.deleteCategoriesRelations = conn
				.prepareStatement("DELETE FROM CATEGORY_FINDING_TYPE_RELTN");
		this.insertCategoryFindingTypeRelation = conn
				.prepareStatement("INSERT INTO CATEGORY_FINDING_TYPE_RELTN (CATEGORY_ID,FINDING_TYPE_ID) VALUES (?,?)");
		this.checkUnassignedArtifactTypes = conn
				.prepareStatement("SELECT A.MNEMONIC FROM ARTIFACT_TYPE A LEFT OUTER JOIN ARTIFACT_TYPE_FINDING_TYPE_RELTN AR ON AR.ARTIFACT_TYPE_ID = A.ID WHERE AR.ARTIFACT_TYPE_ID IS NULL");
		this.checkUncategorizedFindingTypes = conn
				.prepareStatement("SELECT F.NAME FROM FINDING_TYPE F LEFT OUTER JOIN CATEGORY_FINDING_TYPE_RELTN FR ON FR.FINDING_TYPE_ID = F.ID WHERE FR.FINDING_TYPE_ID IS NULL");
	}

	public Long getFindingTypeId(String name) throws SQLException {
		FindingTypeRecord ft = newFindingTypeRecord();
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
			FindingTypeRecord ft = newFindingTypeRecord();
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

	public void updateFindingTypes(FindingTypes type) throws SQLException {
		FindingTypeRecord fRec = newFindingTypeRecord();
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
				if (at.getVersion() != null) {
					insertArtifactTypeFindingTypeRelation.setLong(1,
							getArtifactTypeId(at.getTool(), at.getVersion(), at
									.getMnemonic()));
					insertArtifactTypeFindingTypeRelation.setLong(2, fRec
							.getId());
					insertArtifactTypeFindingTypeRelation.executeUpdate();
				} else {
					for (Long id : getArtifactTypes(at.getTool(), at
							.getMnemonic())) {
						insertArtifactTypeFindingTypeRelation.setLong(1, id);
						insertArtifactTypeFindingTypeRelation.setLong(2, fRec
								.getId());
						insertArtifactTypeFindingTypeRelation.executeUpdate();
					}
				}
			}
		}
		CategoryRecord cRec = newCategoryRecord();
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
					insertCategoryFindingTypeRelation.setLong(1, cRec.getId());
					insertCategoryFindingTypeRelation.setLong(2, fRec.getId());
					insertCategoryFindingTypeRelation.execute();
				} else {
					log.log(Level.SEVERE,
							"Could not locate a finding type for record with uid "
									+ ftId);
				}

			}
		}
	}

	private FindingTypeRecord newFindingTypeRecord() {
		return new FindingTypeRecord(findingTypeMapper);
	}

	private CategoryRecord newCategoryRecord() {
		return new CategoryRecord(categoryMapper);
	}

	public static FindingTypeManager getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}

}
