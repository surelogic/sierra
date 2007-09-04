package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Settings;

public class FindingTypeManager {

	private final PreparedStatement selectFindingType;
	private final PreparedStatement selectFindingTypesByToolMnemonic;
	private final PreparedStatement selectAllFindingTypes;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.selectFindingType = conn
				.prepareStatement("SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND T.VERSION = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?");
		this.selectFindingTypesByToolMnemonic = conn
				.prepareStatement("SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?");
		this.selectAllFindingTypes = conn
				.prepareStatement("SELECT T.NAME, T.VERSION, FT.MNEMONIC,FT.CATEGORY,FT.MNEMONIC_DISPLAY FROM TOOL T, FINDING_TYPE FT WHERE FT.TOOL_ID = T.ID ORDER BY T.NAME,T.VERSION");
	}

	public Long getFindingTypeId(String tool, String version, String mnemonic)
			throws SQLException {
		selectFindingType.setString(1, tool);
		selectFindingType.setString(2, version);
		selectFindingType.setString(3, mnemonic);
		ResultSet set = selectFindingType.executeQuery();
		if (set.next()) {
			return set.getLong(1);
		} else {
			return null;
		}
	}

	public Map<String, Map<String, Collection<FindingTypeDisplay>>> getFindingTypes()
			throws SQLException {
		Map<String, Map<String, Collection<FindingTypeDisplay>>> retMap = new HashMap<String, Map<String, Collection<FindingTypeDisplay>>>();
		ResultSet set = selectAllFindingTypes.executeQuery();
		String tool = null;
		String version = null;
		Collection<FindingTypeDisplay> fts = null;
		while (set.next()) {
			int idx = 1;
			String nextTool = set.getString(idx++);
			String nextVersion = set.getString(idx++);
			if (!nextTool.equals(tool) || !nextVersion.equals(version)) {
				tool = nextTool;
				version = nextVersion;
				fts = new ArrayList<FindingTypeDisplay>();
				Map<String, Collection<FindingTypeDisplay>> versionMap = retMap
						.get(tool);
				if (versionMap == null) {
					versionMap = new HashMap<String, Collection<FindingTypeDisplay>>();
					retMap.put(tool, versionMap);
				}
				versionMap.put(version, fts);
			}
			fts.add(new FindingTypeDisplay(new FindingTypeKey(tool, version,
					set.getString(idx++)), set.getString(idx++), set
					.getString(idx++)));
		}
		return retMap;
	}

	public MessageFilter getMessageFilter(Settings settings)
			throws SQLException {
		Collection<FindingTypeFilter> filters = settings.getRuleFilter();
		Map<Long, FindingTypeFilter> map = new HashMap<Long, FindingTypeFilter>(
				filters.size());
		for (FindingTypeFilter filter : filters) {
			selectFindingTypesByToolMnemonic.setString(1, filter.getTool());
			selectFindingTypesByToolMnemonic.setString(2, filter.getMnemonic());
			ResultSet set = selectFindingTypesByToolMnemonic.executeQuery();
			while (set.next()) {
				map.put(set.getLong(1), filter);
			}
		}
		return new MessageFilter(map);
	}

	public static FindingTypeManager getInstance(Connection conn)
			throws SQLException {
		return new FindingTypeManager(conn);
	}
}
