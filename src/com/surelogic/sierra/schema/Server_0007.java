package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.server.Server;
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
			final long revision = Server.nextRevision(conn);
			sMan.writeFilterSet(set, revision);
		}
		conn.commit();
	}

}
