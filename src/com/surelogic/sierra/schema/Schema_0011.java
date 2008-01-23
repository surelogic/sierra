package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.settings.SettingsManager;

public class Schema_0011 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		final SettingsManager sMan = SettingsManager.getInstance(c);
		sMan.writeGlobalSettingsUUID(new ArrayList<String>(SettingsManager
				.getSureLogicDefaultFilterSet()));
	}

}
