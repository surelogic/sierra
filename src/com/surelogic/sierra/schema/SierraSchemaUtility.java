package com.surelogic.sierra.schema;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.FutureDatabaseException;
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
	public static final int schemaVersion = 19;

	public static final String SQL_SCRIPT_PREFIX = "/com/surelogic/sierra/schema/";
	public static final String SQL_SCRIPT_SUFFIX = ".sql";
	public static final String SERVER_PREFIX = "server";
	public static final String ACTION_PREFIX = "com.surelogic.sierra.schema.";
	public static final String ACTION_COMMON = ACTION_PREFIX + "Schema_";
	public static final String ACTION_SERVER = ACTION_PREFIX + "Server_";
	public static final String SEPARATOR = "_";

	public static void checkAndUpdate(final Connection c, final boolean serverDB)
			throws SQLException, IOException, FutureDatabaseException {
		final int arrayLength = schemaVersion + 1;
		final DBType db = JDBCUtils.getDb(c);
		final URL[] scripts = new URL[arrayLength];
		final SchemaAction[] schemaActions = new SchemaAction[arrayLength];
		for (int i = 0; i < scripts.length; i++) {
			final String num = getZeroPadded(i);
			final Schema common = new Schema(db, num, false);
			scripts[i] = common.script;
			
			schemaActions[i] = common.action;
			if (serverDB) {
			  final Schema server = new Schema(db, num, true);
				final boolean serverScriptOrAction = server.script != null
						|| server.action != null;
				if (serverScriptOrAction) {
				  final SchemaAction commonAction = common.action;
	        final URL serverScript = server.script;
	        final SchemaAction serverAction = server.action;
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
			} else if (common.script == null && common.action == null) { 
			  // not server, and there's no common script/action
			  // Make sure that there's a server script/action
			  final Schema server = new Schema(db, num, true);
			  final boolean serverScriptOrAction = server.script != null ||
                                             server.action != null;
			  if (serverScriptOrAction) {
			    // Dummy action to satisfy constraint in SchemaUtility.checkAndUpdate()
			    // that there be an action or script for each version
			    schemaActions[i] = new SchemaAction() {
			      public void run(Connection c) throws SQLException {
			        System.out.println("Nothing to do in client; only server-side changes for version "+num);  
			      }
			    };
			  } else {
			    throw new IllegalArgumentException("No scripts/actions for version "+num);
			  }
			}
		}
		SchemaUtility.checkAndUpdate(c, scripts, schemaActions);
	}

	private static class Schema {
	  //final boolean forServer;
	  //final String num;
	  final URL script;
	  final SchemaAction action;
	  
	  Schema(DBType db, String num, boolean server) {
	    //this.num = num;
	    //forServer = server;
	    
	    final StringBuilder name = new StringBuilder(SQL_SCRIPT_PREFIX);
	    name.append(db.getPrefix());
	    if (server) {
	      name.append(SEPARATOR).append(SERVER_PREFIX);
	    }
      name.append(SEPARATOR).append(num).append(".sql");
	    script = SierraSchemaUtility.class.getResource(name.toString());
	    
	    final String prefix = server ? ACTION_SERVER : ACTION_COMMON;
	    action = getSchemaAction(prefix + num);
	  }
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
