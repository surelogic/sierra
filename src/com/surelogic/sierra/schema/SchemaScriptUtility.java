package com.surelogic.sierra.schema;

import java.net.URL;

public final class SchemaScriptUtility {

	private SchemaScriptUtility() {
		// no instances
	}

	public static final String DATABASE_SQL = "/com/surelogic/sierra/schema/schema01.sql";

	public static URL getDatabaseSQL() {
		final URL result = SchemaScriptUtility.class.getResource(DATABASE_SQL);
		return result;
	}

}
