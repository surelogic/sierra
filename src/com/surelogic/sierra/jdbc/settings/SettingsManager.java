package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.surelogic.sierra.jdbc.record.FindingTypeFilterRecord;
import com.surelogic.sierra.jdbc.record.FindingTypeRecord;
import com.surelogic.sierra.jdbc.tool.FindingTypeRecordFactory;
import com.surelogic.sierra.tool.message.FindingTypeFilter;
import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Settings;

abstract class SettingsManager {

	protected final Connection conn;
	protected final MessageWarehouse mw;
	protected final FindingTypeRecordFactory ftFactory;

	protected SettingsManager(Connection conn) throws SQLException {
		this.conn = conn;
		mw = MessageWarehouse.getInstance();
		ftFactory = FindingTypeRecordFactory.getInstance(conn);
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
		boolean hasImportance;
		boolean hasFiltered;
		int delta = set.getInt(idx++);
		int importance = set.getInt(idx++);
		hasImportance = !set.wasNull();
		if (hasImportance) {
			filter.setImportance(Importance.values()[importance]);
		}
		String filtered = set.getString(idx++);
		hasFiltered = !set.wasNull();
		if (hasFiltered) {
			filter.setFiltered("Y".equals(filtered));
		}
		if (!(hasImportance || hasFiltered)) {
			filter.setDelta(delta);
		}
		return filter;
	}

	protected void applyFilters(Long entityId, List<FindingTypeFilter> filters)
			throws SQLException {
		for (FindingTypeFilter filter : filters) {
			FindingTypeRecord ftRec = ftFactory.newFindingTypeRecord();
			ftRec.setUid(filter.getName());
			if (ftRec.select()) {
				FindingTypeFilterRecord rec = newFilterRecord();
				rec.setId(new FindingTypeFilterRecord.PK(entityId, ftRec
						.getId()));
				if ((filter.getImportance() != null)
						|| (filter.isFiltered() != null)
						|| ((filter.getDelta() != null) && (filter.getDelta() != 0))) {
					PreparedStatement st = (filter.isFiltered() != null) ? getDeleteFilteredFilterByFindingType()
							: getDeleteImportanceDeltaFiltersByFindingType();
					st.setLong(1, entityId);
					st.setLong(2, ftRec.getId());
					st.execute();
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

	protected abstract PreparedStatement getDeleteImportanceDeltaFiltersByFindingType();

	protected abstract PreparedStatement getDeleteFilteredFilterByFindingType();

	protected abstract FindingTypeFilterRecord newFilterRecord();

}
