package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.settings.CategoryView;
import com.surelogic.sierra.jdbc.settings.ServerSettingsManager;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.Category;
import com.surelogic.sierra.tool.message.FilterEntry;
import com.surelogic.sierra.tool.message.FilterSet;

public class Server_0007 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		final FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		final ServerSettingsManager sMan = ServerSettingsManager
				.getInstance(conn);
		final List<CategoryView> categories = ftMan.listCategories();
		for (final CategoryView c : categories) {
			final Category category = ftMan.getCategory(c.getUid());
			final List<String> findingTypes = category.getFindingType();
			final FilterSet set = new FilterSet();
			set.setName(c.getName());
			set.setUid(UUID.randomUUID().toString());
			final List<FilterEntry> entries = set.getFilter();
			for (final String type : findingTypes) {
				final FilterEntry entry = new FilterEntry();
				entry.setType(type);
				entry.setFiltered(false);
				entries.add(entry);
			}
			PreparedStatement st;
			if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
				st = conn.prepareStatement(
						"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
						new String[] { "REVISION" });
			} else {
				st = conn.prepareStatement(
						"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
						Statement.RETURN_GENERATED_KEYS);
			}
			st.setTimestamp(1, new Timestamp(new Date().getTime()));
			st.execute();
			ResultSet rSet = st.getGeneratedKeys();
			try {
				rSet.next();
				final long revision = rSet.getLong(1);
				sMan.createFilterSet(set.getName(), null, revision);
				sMan.updateFilterSet(set, rSet.getLong(1));
			} finally {
				rSet.close();
			}
		}
		conn.commit();
	}

}
