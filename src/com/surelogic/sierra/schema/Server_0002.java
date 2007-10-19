package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.JDBCUtils;
import com.surelogic.sierra.jdbc.finding.ServerFindingManager;

public class Server_0002 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		c.createStatement().execute("DELETE FROM TIME_SERIES_OVERVIEW");
		c.createStatement().execute("DELETE FROM SCAN_OVERVIEW");
		c.createStatement().execute("DELETE FROM SCAN_SUMMARY");
		c.commit();
		ResultSet scan = c
				.createStatement()
				.executeQuery(
						"SELECT P.NAME, S.UUID FROM SCAN S, PROJECT P WHERE P.ID = S.PROJECT_ID");
		ServerFindingManager man = ServerFindingManager.getInstance(c);
		Statement st = c.createStatement();
		while (scan.next()) {
			String project = scan.getString(1);
			String uid = scan.getString(2);
			Set<String> qualifiers = new HashSet<String>();
			ResultSet qSet = st
					.executeQuery("SELECT Q.NAME FROM SCAN S, QUALIFIER_SCAN_RELTN QSR, QUALIFIER Q WHERE S.UUID = "
							+ JDBCUtils.escapeString(uid)
							+ " AND QSR.SCAN_ID = S.ID AND Q.ID = QSR.QUALIFIER_ID");
			while(qSet.next()) {
				qualifiers.add(qSet.getString(1));
			}
			man.generateOverview(project, uid, qualifiers);
			c.commit();
		}
	}

}
