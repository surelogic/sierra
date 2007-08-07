package com.surelogic.sierra.schema;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.common.jdbc.SchemaUtility;

public final class SierraSchemaUtility {

	private SierraSchemaUtility() {
		// no instances
	}

	/**
	 * Up this number when you add a new schema version SQL script to this
	 * package.
	 */
	public static final int schemaVersion = 0;

	public static final String SQL_SCRIPT_PREFIX = "/com/surelogic/sierra/schema/schema_";
	public static final String SCHEMA_ACTION_PREFIX = "com.surelogic.sierra.schema.Schema_";

	public static void checkAndUpdate(final Connection c) throws SQLException,
			IOException {
		final int arrayLength = schemaVersion + 1;

		final URL[] scripts = new URL[arrayLength];
		final SchemaAction[] schemaActions = new SchemaAction[arrayLength];
		for (int i = 0; i < scripts.length; i++) {
			scripts[i] = SierraSchemaUtility.class
					.getResource(SQL_SCRIPT_PREFIX + getZeroPadded(i) + ".sql");
			try {
				schemaActions[i] = (SchemaAction) Class.forName(
						SCHEMA_ACTION_PREFIX + getZeroPadded(i)).newInstance();
			} catch (InstantiationException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			} catch (ClassNotFoundException e) {
				// It is okay to not have any jobs for this version, do
				// nothing.
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (SecurityException e) {
				throw new IllegalStateException(e);
			}
		}
		SchemaUtility.checkAndUpdate(c, scripts, schemaActions);
	}

	/**
	 * Pads the given positive integer with 0s and returns a string of at least
	 * 4 characters. For example: <code>getZeroPadded(0)</code> results in the
	 * string <code>"0000"</code>; <code>getZeroPadded(436)</code> results
	 * in the string <code>"0456"</code>; <code>getZeroPadded(56900)</code>
	 * results in the string <code>"56900"</code>.
	 * 
	 * @param n
	 *            a non-negative integer (i.e., n >=0).
	 * @return a
	 */
	private static String getZeroPadded(final int n) {
		assert n >= 0;

		String result = "" + n;
		while (result.length() < 4)
			result = "0" + result;
		return result;
	}
}
