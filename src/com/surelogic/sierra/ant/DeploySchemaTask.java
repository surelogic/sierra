package com.surelogic.sierra.ant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * Deploy the SPS schema to a database. The following properties affect this
 * task:
 * 
 * <dl>
 * <li>sierra.db.type - Accepts <tt>oracle</tt> or <tt>derby</tt>. The
 * default is <tt>derby</tt>.
 * <li>sierra.db.url - The full jdbc url connection string, e.g.,
 * <tt>jdbc:oracle:thin:@localhost:1521:xe</tt> or (the default)
 * <tt>jdbc:derby://localhost:1527/SIERRA;user=SIERRA</tt>
 * <li>sierra.db.user - The database user, defaults to <tt>sierra</tt></li>
 * <li>sierra.db.pass - The database password defaults to <tt>sierra</tt></li>
 * </dl>
 * 
 * @author nathan
 * 
 */
public class DeploySchemaTask {

	private static final String DERBYDRIVER = "org.apache.derby.jdbc.ClientDriver";
	private static final String ORACLEDRIVER = "oracle.jdbc.driver.OracleDriver";

	private final Logger log = SLLogger.getLoggerFor(DeploySchemaTask.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeploySchemaTask().execute();
	}

	public void execute() {
		String type = System.getProperty("sierra.db.type", "derby");
		String user = null;
		String pass = null;

		user = System.getProperty("sierra.db.user", "sierra");
		pass = System.getProperty("sierra.db.pass", "sierra");

		try {
			if ("oracle".equals(type)) {
				Class.forName(ORACLEDRIVER);
			} else {
				Class.forName(DERBYDRIVER);
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Could not locate derby client driver", e);
		}

		String url = System.getProperty("sierra.db.url",
				"jdbc:derby://localhost:1527/SIERRA;user=SIERRA");
		try {
			Connection conn;
			if ("oracle".equals(type)) {
				conn = DriverManager.getConnection(url, user, pass);
			} else {
				conn = DriverManager.getConnection(url);
			}
			conn.setAutoCommit(false);
			log.info("Database is " + JDBCUtils.getDb(conn));
			SierraSchemaUtility.checkAndUpdate(conn, true);
			ServerFindingManager man = ServerFindingManager.getInstance(conn);
			Statement st = conn.createStatement();
			conn.commit();
			ResultSet set = st
					.executeQuery("SELECT S.ID,Q.QUALIFIER_ID,S.PROJECT_ID,S.SCAN_DATE_TIME FROM SCAN S, QUALIFIER_SCAN_RELTN Q WHERE Q.SCAN_ID = S.ID ORDER BY Q.QUALIFIER_ID,S.SCAN_DATE_TIME");
			while (set.next()) {
				int idx = 1;
				long scanId = set.getLong(idx++);
				long qualifierId = set.getLong(idx++);
				long projectId = set.getLong(idx++);
				Timestamp time = set.getTimestamp(idx++);
				log.info("Populating scan summary for (" + scanId + ","
						+ qualifierId + "," + projectId + "," + time + ")");
				man.refreshScanSummary(scanId, qualifierId, projectId);
				conn.commit();
			}
			System.out.println("done");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
