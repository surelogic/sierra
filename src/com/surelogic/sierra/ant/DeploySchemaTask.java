package com.surelogic.sierra.ant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * Deploy the SPS schema to a database. Currently, the target database must be a
 * derby database. The database does not need to exist before-hand. The
 * following properties affect this task:
 * 
 * <dl>
 * <li>sierra.db.type - Accepts <code>oracle</code> or <code>derby</code>.
 * The default is derby.
 * <li>sierra.db.url - The full jdbc url connection string (i.e.
 * "jdbc:oracle:thin:@localhost:1527:sierra" or
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
//			conn.createStatement().execute(
//					"INSERT INTO QUALIFIER(NAME) VALUES ('Default')");
			conn.commit();

			// ServerFindingManager man =
			// ServerFindingManager.getInstance(conn);
			// ResultSet set = conn
			// .createStatement()
			// .executeQuery(
			// "SELECT P.NAME, S.UID FROM SCAN S, PROJECT P WHERE P.ID =
			// S.PROJECT_ID");
			// while (set.next()) {
			// man.generateOverview(set.getString(1), set.getString(2),
			// new TreeSet<String>(Arrays
			// .asList(new String[] { "Default" })));
			// conn.commit();
			// }
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
