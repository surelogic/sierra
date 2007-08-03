package com.surelogic.sierra.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Deploy the SPS schema to a database. Currently, the target database must be a
 * derby database. The database does not need to exist before-hand. The
 * following properties affect this task:
 * 
 * <dl>
 * <li>sierra.db.location - The root directory (or host) containing derby database
 * instances.</li>
 * <li>sierra.db.name - The name of the database</li>
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
		//TODO boot schema
	}

}
