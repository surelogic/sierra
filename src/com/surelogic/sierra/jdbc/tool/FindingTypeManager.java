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

import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.tool.message.ArtifactType;
import com.surelogic.sierra.tool.message.FindingType;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.Settings;

public class FindingTypeManager {

	private final PreparedStatement selectArtifactTypesByFindingType;
	private final PreparedStatement selectArtifactType;
	private final PreparedStatement selectArtifactTypesByToolAndMnemonic;
	private final PreparedStatement deleteArtifactTypesByFindingType;
	private final PreparedStatement insertArtifactTypeFindingTypeRelation;
	private final UpdateBaseMapper findingTypeMapper;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.selectArtifactType = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND T.VERSION = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.selectArtifactTypesByFindingType = conn
				.prepareStatement("SELECT AR.ID FROM FINDING_TYPE FT, ARTIFACT_TYPE_FINDING_TYPE_RELTN AFTR, ARTIFACT_TYPE AR WHERE FT.NAME = ? AND AFTR.FINDING_TYPE_ID = FT.ID AND AR.ID = AFTR.ARTIFACT_TYPE_ID");
		this.selectArtifactTypesByToolAndMnemonic = conn
				.prepareStatement("SELECT AR.ID FROM TOOL T, ARTIFACT_TYPE AR WHERE T.NAME = ? AND AR.TOOL_ID = T.ID AND AR.MNEMONIC = ?");
		this.deleteArtifactTypesByFindingType = conn
				.prepareStatement("DELETE FROM ARTIFACT_TYPE_FINDING_TYPE_RELTN WHERE FINDING_TYPE_ID = ?");
		this.insertArtifactTypeFindingTypeRelation = conn
				.prepareStatement("INSERT INTO ARTIFACT_TYPE_FINDING_TYPE_RELTN (ARTIFACT_TYPE_ID,FINDING_TYPE_ID) VALUES (?,?)");
		this.findingTypeMapper = new UpdateBaseMapper(
				conn,
				"INSERT INTO FINDING_TYPE (NAME,SHORT_MESSAGE,INFO) VALUES (?,?,?)",
				"SELECT ID,SHORT_MESSAGE,INFO FROM FINDING_TYPE WHERE NAME = ?",
				"DELETE FROM FINDING_TYPE WHERE ID = ?",
				"UPDATE FINDING_TYPE SET SHORT_MESSAGE = ?, INFO = ? WHERE ID = ?");
	}

	public Long getFindingTypeId(String name) throws SQLException {
		FindingTypeRecord ft = newRecord();
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
			FindingTypeRecord ft = newRecord();
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
		FindingTypeRecord rec = newRecord();
		for (FindingType ft : type.getFindingType()) {
			rec.setName(ft.getName());
			rec.setInfo(ft.getInfo());
			rec.setShortMessage(ft.getShortMessage());
			if (rec.select()) {
				rec.update();
				deleteArtifactTypesByFindingType.setLong(1, rec.getId());
				deleteArtifactTypesByFindingType.executeUpdate();
			} else {
				rec.insert();
			}
			for (ArtifactType at : ft.getArtifact()) {
				if (at.getVersion() != null) {
					insertArtifactTypeFindingTypeRelation.setLong(1,
							getArtifactTypeId(at.getTool(), at.getVersion(), at
									.getMnemonic()));
					insertArtifactTypeFindingTypeRelation.setLong(2, rec
							.getId());
					insertArtifactTypeFindingTypeRelation.executeUpdate();
				} else {
					for (Long id : getArtifactTypes(at.getTool(), at
							.getMnemonic())) {
						insertArtifactTypeFindingTypeRelation.setLong(1, id);
						insertArtifactTypeFindingTypeRelation.setLong(2, rec
								.getId());
						insertArtifactTypeFindingTypeRelation.executeUpdate();
					}
				}
			}
		}
	}

	public static FindingTypeManager getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}

	private FindingTypeRecord newRecord() {
		return new FindingTypeRecord(findingTypeMapper);
	}

}
