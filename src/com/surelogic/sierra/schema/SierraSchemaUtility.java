package com.surelogic.sierra.schema;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.common.jdbc.SchemaUtility;
import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

public final class SierraSchemaUtility {

	private SierraSchemaUtility() {
		// no instances
	}

	/**
	 * Up this number when you add a new schema version SQL script to this
	 * package.
	 */
	public static final int schemaVersion = 12;

	public static final String SQL_SCRIPT_PREFIX = "/com/surelogic/sierra/schema/";
	public static final String SQL_SCRIPT_SUFFIX = ".sql";
	public static final String SERVER_PREFIX = "server";
	public static final String ACTION_PREFIX = "com.surelogic.sierra.schema.";
	public static final String ACTION_COMMON = ACTION_PREFIX + "Schema_";
	public static final String ACTION_SERVER = ACTION_PREFIX + "Server_";
	public static final String SEPARATOR = "_";

	public static void checkAndUpdate(final Connection c, final boolean serverDB)
			throws SQLException, IOException {
		final int arrayLength = schemaVersion + 1;
		final DBType db = JDBCUtils.getDb(c);
		final URL[] scripts = new URL[arrayLength];
		final SchemaAction[] schemaActions = new SchemaAction[arrayLength];
		for (int i = 0; i < scripts.length; i++) {
			final String num = getZeroPadded(i);
			scripts[i] = SierraSchemaUtility.class
					.getResource(SQL_SCRIPT_PREFIX + db.getPrefix() + SEPARATOR
							+ num + SQL_SCRIPT_SUFFIX);
			final SchemaAction commonAction = getSchemaAction(ACTION_COMMON
					+ num);
			schemaActions[i] = commonAction;
			if (serverDB) {
				final URL serverScript = SierraSchemaUtility.class
						.getResource(SQL_SCRIPT_PREFIX + db.getPrefix()
								+ SEPARATOR + SERVER_PREFIX + SEPARATOR + num
								+ ".sql");
				final SchemaAction serverAction = getSchemaAction(ACTION_SERVER
						+ num);
				final boolean serverScriptOrAction = serverScript != null
						|| serverAction != null;
				if (serverScriptOrAction) {
					schemaActions[i] = new SchemaAction() {
						public void run(Connection c) throws SQLException {
							/*
							 * This approach is very generic, the server may or
							 * may not define schema scripts and/or actions at
							 * each schema level. We only run them if they
							 * exist.
							 */
							if (commonAction != null) {
								SchemaUtility.runAction(commonAction, c);
							}

							if (serverScript != null) {
								final Statement st = c.createStatement();
								try {
									SchemaUtility.runScript(serverScript, st);
								} catch (IOException e) {
									throw new SQLException(
											"IOException reading server script file "
													+ serverScript.getFile()
													+ " : " + e.toString());
								} finally {
									st.close();
								}
							}

							if (serverAction != null) {
								SchemaUtility.runAction(serverAction, c);
							}
						}
					};
				}
			}
		}
		SchemaUtility.checkAndUpdate(c, scripts, schemaActions);
	}

	private static SchemaAction getSchemaAction(
			final String fullyQualifiedClassName) {
		SchemaAction result = null;
		try {
			result = (SchemaAction) Class.forName(fullyQualifiedClassName)
					.newInstance();
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
		return result;
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

		String result = Integer.toString(n);
		while (result.length() < 4) {
			result = "0" + result;
		}
		return result;
	}
}
