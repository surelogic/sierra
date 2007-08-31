package com.surelogic.sierra.jdbc.settings;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.tool.message.MessageWarehouse;

abstract class SettingsManager {

	protected final Connection conn;
	protected final MessageWarehouse mw;

	protected SettingsManager(Connection conn) throws SQLException {
		this.conn = conn;
		mw = MessageWarehouse.getInstance();
	}

}
