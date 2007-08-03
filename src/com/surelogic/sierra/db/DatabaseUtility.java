package com.surelogic.sierra.db;

import java.net.URL;

public final class DatabaseUtility {

	private DatabaseUtility() {
		// no instances
	}

	public static final String DATABASE_SQL = "/com/surelogic/sierra/db/database.sql";

	public static URL getDatabaseSQL() {
		final URL result = DatabaseUtility.class.getResource(DATABASE_SQL);
		return result;
	}

}
