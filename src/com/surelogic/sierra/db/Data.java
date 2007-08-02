package com.surelogic.sierra.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.surelogic.common.eclipse.Derby;
import com.surelogic.sierra.findbugs.FindBugsToolInfoGenerator;
import com.surelogic.sierra.pmd.PMDToolInfoGenerator;
import com.surelogic.sierra.tool.SierraLogger;

public final class Data {

	public static final String DATABASE_SQL = "/com/surelogic/sierra/db/database.sql";

	public static URL getDatabaseSQL() {
		final URL result = Data.class.getResource(DATABASE_SQL);
		return result;
	}

	private static final String SCHEMA_VERSION = "1.0";

	private static final String JDBC_PRE = "jdbc:derby:";

	private static final String DATABASE_HOME_PROP = "sierra.db.location";

	private static final String DATABASE_NAME_PROP = "sierra.db.name";

	private static final String DATABASE_PASS_PROP = "sierra.db.password";

	private static final String DATABASE_USER_PROP = "sierra.db.user";

	private static final Logger log = SierraLogger.getLogger("Sierra");

	public static void bootAndCheckSchema(final URL schemaURL) {
		assert schemaURL != null;

        Derby.bootEmbedded();

		final String connectionURL = getBootstrapConnectionURL();
		try {
			boolean schemaExists = false; // assume the worst
			final Connection c = DriverManager.getConnection(connectionURL);
			try {
				final Statement st = c.createStatement();
				try {
					final ResultSet ver = st
							.executeQuery("select SIERRA from SIERRA.VERSION");
					String schemaVer = "NONE";
					while (ver.next()) {
						schemaVer = ver.getString(1);
					}
					if (schemaVer.equals(SCHEMA_VERSION)) {
						schemaExists = true;
						log.info("Schema (version " + SCHEMA_VERSION
								+ ") exists in the embedded SIERRA database "
								+ getConnectionURL() + ".");
					}
				} catch (SQLException e) {
					/*
					 * Ignore, this exception occurred because the schema was
					 * not found within the embedded database.
					 */
				} finally {
					st.close();
				}
				if (!schemaExists) {
					loadSchema(c, schemaURL);
					loadTools();
					log.info("Schema (version " + SCHEMA_VERSION
							+ ") created in the embedded SIERRA database "
							+ getConnectionURL() + ".");
				}
			} finally {
				c.close();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(getConnectionURL());
	}

	private static void loadSchema(final Connection c, final URL schemaURL)
			throws SQLException {
		assert c != null;
		assert schemaURL != null;

		List<StringBuilder> stmts = getSQLStatements(schemaURL);
		final Statement st = c.createStatement();
		try {
			for (StringBuilder b : stmts) {
				st.executeUpdate(b.toString());
			}
		} finally {
			st.close();
		}
	}

	private static void loadTools() {
		Connection conn;
		try {
			conn = getConnection();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		PMDToolInfoGenerator.generateTool(conn);
		FindBugsToolInfoGenerator.generateTool(conn);
	}

	private static List<StringBuilder> getSQLStatements(final URL schemaURL) {
		assert schemaURL != null;

		List<StringBuilder> result = new ArrayList<StringBuilder>();

		try {
			final InputStream is = schemaURL.openStream();
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);

			try {
				StringBuilder b = new StringBuilder();
				String buffer;
				while ((buffer = br.readLine()) != null) {
					buffer = buffer.trim();
					if (buffer.startsWith("--") || buffer.equals("")) {
						// comment or blank line -- ignore this line
					} else if (buffer.endsWith(";")) {
						// end of an SQL statement -- add to our resulting list
						if (b.length() > 0)
							b.append("\n");
						b.append(buffer);
						b.deleteCharAt(b.length() - 1); // remove the ";"
						result.add(b);
						b = new StringBuilder();
					} else {
						// add this line (with a newline after the first line)
						if (b.length() > 0)
							b.append("\n");
						b.append(buffer);
					}
				}
				br.readLine();
			} finally {
				br.close();
			}
		} catch (IOException e) {
			throw new IllegalStateException(
					"Unable to open/read the SIERRA schema file: " + schemaURL,
					e);
		}
		return result;
	}

	private static String getBootstrapConnectionURL() {
		String home = System.getProperty(DATABASE_HOME_PROP);
		String name = System.getProperty(DATABASE_NAME_PROP);
		String user = System.getProperty(DATABASE_USER_PROP);
		String pass = System.getProperty(DATABASE_PASS_PROP);

		String url = JDBC_PRE
				+ (home == null ? "" : home
						+ System.getProperty("file.separator"))
				+ (name == null ? "sierra" : name)
				+ (user == null ? ";user=sierra" : (";user=" + user))
				+ (pass == null ? "" : (";password=" + pass)) + ";create=true";
		return url;
	}

	public static String getConnectionURL() {
		String home = System.getProperty(DATABASE_HOME_PROP);
		String name = System.getProperty(DATABASE_NAME_PROP);
		String user = System.getProperty(DATABASE_USER_PROP);
		String pass = System.getProperty(DATABASE_PASS_PROP);
		String url = JDBC_PRE
				+ (home == null ? "" : home
						+ System.getProperty("file.separator"))
				+ (name == null ? "sierra" : name)
				+ (user == null ? ";user=sierra" : (";user=" + user))
				+ (pass == null ? "" : (";password=" + pass));
		return url;
	}
}
