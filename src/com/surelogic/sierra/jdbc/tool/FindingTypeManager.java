package com.surelogic.sierra.jdbc.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Settings;

public class FindingTypeManager {

	private final PreparedStatement selectFindingType;
	private final PreparedStatement selectFindingTypesByToolMnemonic;

	private FindingTypeManager(Connection conn) throws SQLException {
		this.selectFindingType = conn
				.prepareStatement("SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND T.VERSION = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?");
		this.selectFindingTypesByToolMnemonic = conn
				.prepareStatement("SELECT FT.ID FROM TOOL T, FINDING_TYPE FT WHERE T.NAME = ? AND FT.TOOL_ID = T.ID AND FT.MNEMONIC = ?");
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
