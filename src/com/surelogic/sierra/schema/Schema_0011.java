package com.surelogic.sierra.schema;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.settings.SettingsManager;
import com.surelogic.sierra.jdbc.tool.FindingTypeManager;
import com.surelogic.sierra.tool.message.FindingTypes;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class Schema_0011 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		MessageWarehouse mw = MessageWarehouse.getInstance();
		FindingTypeManager ftMan = FindingTypeManager.getInstance(c);
		List<FindingTypes> types = new ArrayList<FindingTypes>(3);
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/findbugs.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/pmd.xml");
		types.add(mw.fetchFindingTypes(in));
		in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(
						"com/surelogic/sierra/tool/message/checkstyle.xml");
		types.add(mw.fetchFindingTypes(in));
		ftMan.updateFindingTypes(types, 0);
		final SettingsManager sMan = SettingsManager.getInstance(c);
		sMan.writeGlobalSettingsUUID(new ArrayList<String>(SettingsManager
				.getSureLogicDefaultFilterSet()));
	}

}
