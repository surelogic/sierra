package com.surelogic.sierra.schema;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.JDBCUtils;

/**
 * Deploy the Sierra schema to a database. The below system properties specifiy
 * the database this task is performed upon. Note that the database must be
 * empty or the task will fail (for Derby just delete the files).
 * 
 * <dl>
 * <li>sierra.db.type - Accepts <tt>oracle</tt> or <tt>derby</tt>. The
 * default is <tt>derby</tt>.
 * <li>sierra.db.url - The full jdbc url connection string, e.g.,
 * <tt>jdbc:oracle:thin:@localhost:1521:xe</tt> or (the default)
 * <tt>jdbc:derby://localhost:1527/SIERRA;create=true;user=SIERRA</tt>
 * <li>sierra.db.user - The database user, defaults to <tt>sierra</tt></li>
 * <li>sierra.db.pass - The database password defaults to <tt>sierra</tt></li>
 * </dl>
 * 
 * @author nathan
 */
public class DeploySchemaTask {

	private static final String DERBYDRIVER = "org.apache.derby.jdbc.ClientDriver";
	private static final String ORACLEDRIVER = "oracle.jdbc.driver.OracleDriver";

	private static final Logger log = SLLogger.getLoggerFor(DeploySchemaTask.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeploySchemaTask().execute();
	}

	public void execute() {
		final String dbType = System.getProperty("sierra.db.type", "derby");
		if (!("oracle".equals(dbType) || "derby".equals(dbType))) {
			throw new IllegalArgumentException("sierra.db.type=\"" + dbType
					+ "\" is not either \"derby\" or \"oracle\"");
		}
		final String dbUser = System.getProperty("sierra.db.user", "sierra");
		final String dbPass = System.getProperty("sierra.db.pass", "sierra");

		try {
			if ("oracle".equals(dbType)) {
				Class.forName(ORACLEDRIVER);
			} else {
				Class.forName(DERBYDRIVER);
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Could not locate derby client driver", e);
		}

		String url = System.getProperty("sierra.db.url",
				"jdbc:derby://localhost:1527/SIERRA;create=true;user=SIERRA");

		log.info("Deploying Sierra schema to the " + dbType + " database at "
				+ url + " [" + dbUser + "]");
		try {
			Connection conn;
			if ("oracle".equals(dbType)) {
				conn = DriverManager.getConnection(url, dbUser, dbPass);
			} else {
				conn = DriverManager.getConnection(url);
			}
			conn.setAutoCommit(false);
			log.info("Database is " + JDBCUtils.getDb(conn));
			SierraSchemaUtility.checkAndUpdate(conn, true);
			conn.commit();
			log.info("Completed deploying Sierra schema to the " + dbType
					+ " database at " + url + " [" + dbUser + "]");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
