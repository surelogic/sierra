package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Settings;

abstract class SettingsManager {

	protected final Connection conn;
	protected final MessageWarehouse mw;
	protected final FindingTypeManager ftMan;

	protected SettingsManager(Connection conn) throws SQLException {
		this.conn = conn;
		mw = MessageWarehouse.getInstance();
		ftMan = FindingTypeManager.getInstance(conn);
	}

	protected Settings readSettings(ResultSet set) throws SQLException {
		try {
			Settings settings = new Settings();
			List<FindingTypeFilter> filters = settings.getFilter();
			while (set.next()) {
				filters.add(readFilter(set));
			}
			return settings;
		} finally {
			set.close();
		}

	}


	protected FindingTypeFilter readFilter(ResultSet set) throws SQLException {
		int idx = 1;
		FindingTypeFilter filter = new FindingTypeFilter();
		filter.setName(set.getString(idx++));
		int delta = set.getInt(idx++);
		int importance = set.getInt(idx++);
		if (set.wasNull()) {
			filter.setDelta(delta);
		} else {
			filter.setImportance(Importance.values()[importance]);
		}
		filter.setFiltered("Y".equals(set.getString(idx++)));
		return filter;
	}
	
	protected void applyFilters(Long entityId, List<FindingTypeFilter> filters)
			throws SQLException {
		final FindingTypeFilterRecord rec = newFilterRecord();
		for (FindingTypeFilter filter : filters) {
			final Long findingTypeId = ftMan.getFindingTypeId(filter.getName());
			if (findingTypeId != null) {
				rec.setId(new FindingTypeFilterRecord.PK(entityId,
						findingTypeId));
				if ((filter.getImportance() != null)
						|| (filter.isFiltered())
						|| ((filter.getDelta() != null) && (filter.getDelta() != 0))) {
					rec.setImportance(filter.getImportance());
					rec.setFiltered(filter.isFiltered());
					rec.setDelta(filter.getDelta());
					rec.insert();
				}
			} else {
				throw new IllegalArgumentException(filter.getName()
						+ " is not a valid filter name.");
			}
		}
	}

	protected abstract PreparedStatement getDeleteFilterByFindingType();

	protected abstract FindingTypeFilterRecord newFilterRecord();

}
