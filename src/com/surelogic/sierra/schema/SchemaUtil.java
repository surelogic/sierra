package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.*;

public class SchemaUtil {
	static void updateFindingTypes(Connection conn) throws SQLException {
		FindingTypeManager ftMan = FindingTypeManager.getInstance(conn);
		List<FindingTypes> types = new ArrayList<FindingTypes>(3);

		types.add(getFindingTypes("findbugs.xml"));
		types.add(getFindingTypes("pmd.xml"));
		types.add(getFindingTypes("cpd.xml"));
		types.add(getFindingTypes("checkstyle.xml"));
		ftMan.updateFindingTypes(types, 0);
	}

	private static FindingTypes getFindingTypes(String file) {
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/" + file);
		return MessageWarehouse.getInstance().fetchFindingTypes(in);
	}

	static void setupFilters(Connection c) throws SQLException {
		final SettingsManager sMan = SettingsManager.getInstance(c);
		if (sMan.getGlobalSettings().isEmpty()) {
			sMan.writeGlobalSettingsUUID(new ArrayList<String>(SettingsManager
					.getSureLogicDefaultFilterSet()));
		}
	}
}
