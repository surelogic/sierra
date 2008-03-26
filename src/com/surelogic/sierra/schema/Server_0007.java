package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.ConnectionQuery;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.settings.CategoryView;
import com.surelogic.sierra.jdbc.settings.FilterEntryDO;
import com.surelogic.sierra.jdbc.settings.FilterSetDO;
import com.surelogic.sierra.jdbc.settings.FilterSets;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.Category;

public class Server_0007 implements SchemaAction {

	public void run(Connection conn) throws SQLException {
		final FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		final List<CategoryView> categories = ftMan.listCategories();
		for (final CategoryView c : categories) {
			final Category category = ftMan.getCategory(c.getUid());
			final List<String> findingTypes = category.getFindingType();
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
			final ResultSet rSet = st.getGeneratedKeys();
			try {
				rSet.next();
				final long revision = rSet.getLong(1);
				final FilterSets sets = new FilterSets(
						new ConnectionQuery(conn));
				final FilterSetDO filterSet = sets.createFilterSet(c.getName(),
						null, revision);
				final List<FilterEntryDO> entries = filterSet.getFilters();
				for (final String type : findingTypes) {
					final FilterEntryDO entry = new FilterEntryDO(type, false);
					entries.add(entry);
				}
				sets.updateFilterSet(filterSet, rSet.getLong(1));
			} finally {
				rSet.close();
			}
		}
	}

}
