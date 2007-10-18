package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.finding.ClientFindingManager;

public class Schema_0002 implements SchemaAction {

	public void run(Connection c) throws SQLException {
		PreparedStatement selectCompilation = c
				.prepareStatement("SELECT ID FROM COMPILATION_UNIT WHERE PACKAGE_NAME = ? AND CU = ? AND ID != ?");
		PreparedStatement updateCompilation = c
				.prepareStatement("UPDATE COMPILATION_UNIT SET CU = ? WHERE ID = ?");
		PreparedStatement deleteCompilation = c
				.prepareStatement("DELETE FROM COMPILATION_UNIT WHERE ID = ?");
		PreparedStatement updateClass = c
				.prepareStatement("UPDATE SOURCE_LOCATION SET CLASS_NAME = ? WHERE COMPILATION_UNIT_ID = ?");
		PreparedStatement updateSourceCompilation = c
				.prepareStatement("UPDATE SOURCE_LOCATION SET COMPILATION_UNIT_ID = ? WHERE COMPILATION_UNIT_ID = ?");
		Statement st = c.createStatement();
		ResultSet set = st
				.executeQuery("SELECT ID, CU, PACKAGE_NAME FROM COMPILATION_UNIT");
		try {
			while (set.next()) {
				int idx = 1;
				Long id = set.getLong(idx++);
				String className = set.getString(idx++);
				String packageName = set.getString(idx++);
				int strIndex;
				String compilation = className;
				if ((strIndex = className.indexOf("$")) > -1) {
					compilation = className.substring(0, strIndex);
				}
				idx = 1;
				updateClass.setString(idx++, className);
				updateClass.setLong(idx++, id);
				updateClass.execute();
				idx = 1;
				selectCompilation.setString(idx++, packageName);
				selectCompilation.setString(idx++, compilation);
				selectCompilation.setLong(idx++, id);
				ResultSet comp = selectCompilation.executeQuery();
				try {
					if (comp.next()) {
						Long previousId = comp.getLong(1);
						idx = 1;
						updateSourceCompilation.setLong(idx++, previousId);
						updateSourceCompilation.setLong(idx++, id);
						updateSourceCompilation.execute();
						deleteCompilation.setLong(1, id);
						deleteCompilation.execute();
					} else {
						idx = 1;
						updateCompilation.setString(idx++, compilation);
						updateCompilation.setLong(idx++, id);
						updateCompilation.execute();
					}
				} finally {
					comp.close();
				}
			}
		} finally {
			set.close();
		}
		try {
			st.executeQuery("SELECT * FROM SERVER");
		} catch (SQLException e) {
			set = st.executeQuery("SELECT PROJECT,SCAN_UUID FROM LATEST_SCANS");
			try {
				ClientFindingManager man = ClientFindingManager.getInstance(c);
				while (set.next()) {
					man.generateOverview(set.getString(1), set.getString(2));
				}
			} finally {
				set.close();
			}
		}
		c.commit();
	}

}
