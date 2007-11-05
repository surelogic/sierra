package com.surelogic.sierra.ant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * Deploy the SPS schema to a database. The following properties affect this
 * task:
 * 
 * <dl>
 * <li>sierra.db.type - Accepts <code>oracle</code> or <code>derby</code>.
 * The default is derby.
 * <li>sierra.db.url - The full jdbc url connection string (i.e.
 * "jdbc:oracle:thin:@localhost:1521:xe" or
 * "jdbc:derby://localhost:1527/SIERRA;user=SIERRA")
 * <li>sierra.db.user - The database user</li>
 * <li>sierra.db.pass - The database password</li>
 * </dl>
 * 
 * @author nathan
 * 
 */
public class DeploySchemaTask extends Task {

	private static final String DERBYDRIVER = "org.apache.derby.jdbc.ClientDriver";
	private static final String ORACLEDRIVER = "oracle.jdbc.driver.OracleDriver";

	private final Logger log = SLLogger.getLoggerFor(DeploySchemaTask.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeploySchemaTask().execute();
	}

	@Override
	public void execute() throws BuildException {
		String type = System.getProperty("sierra.db.type");
		String user = null;
		String pass = null;

		if (getProject() != null) {
			user = getProject().getProperty("sierra.db.user");
			pass = getProject().getProperty("sierra.db.pass");
		} else {
			user = System.getProperty("sierra.db.user");
			pass = System.getProperty("sierra.db.pass");
		}
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

		String url = System.getProperty("sierra.db.url");
		try {
			Connection conn;
			if ("oracle".equals(type)) {
				conn = DriverManager.getConnection(url, user, pass);
			} else {
				conn = DriverManager.getConnection(url);
			}
			conn.setAutoCommit(false);
			log("Database is " + JDBCUtils.getDb(conn));
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
				man.refreshScanSummary(scanId, qualifierId, projectId, time);
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
