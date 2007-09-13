package com.surelogic.sierra.ant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.surelogic.sierra.schema.SierraSchemaUtility;

/**
 * Deploy the SPS schema to a database. Currently, the target database must be a
 * derby database. The database does not need to exist before-hand. The
 * following properties affect this task:
 * 
 * <dl>
 * <li>sierra.db.location - The url where the database is locatied (ie
 * //localhost:1527/SIERRA)</li>
 * <li>sierra.db.user - The database user</li>
 * <li>sierra.db.pass - The database password</li>
 * </dl>
 * 
 * @author nathan
 * 
 */
public class DeploySchemaTask extends Task {

	private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DeploySchemaTask().execute();
	}

	@Override
	public void execute() throws BuildException {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Could not locate derby client driver", e);
		}

		String user = null;
		String pass = null;
		String location = null;
		
		if (getProject() != null) {
			user = getProject().getProperty("sierra.db.user");
			pass = getProject().getProperty("sierra.db.pass");
			location= getProject().getProperty("sierra.db.location");
		} else {
			user = System.getProperty("sierra.db.user");
			pass = System.getProperty("sierra.db.pass");
			location = System.getProperty("sierra.db.location");
		}
		
		String url = "jdbc:derby:" + ((location == null) ? "" : location)
				+ ((pass == null) ? "" : ";password=" + pass)
				+ ((user == null) ? "" : ";user=" + user) + ";create=true";
		
		try {
			Connection conn = DriverManager.getConnection(url);
			SierraSchemaUtility.checkAndUpdate(conn, true);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
