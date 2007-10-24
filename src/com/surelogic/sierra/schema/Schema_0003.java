package com.surelogic.sierra.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.common.jdbc.SchemaAction;
import com.surelogic.sierra.jdbc.JDBCUtils;

public class Schema_0003 implements SchemaAction {

	public void run(final Connection conn) throws SQLException {
		if (!JDBCUtils.isServer(conn)) {
			PreparedStatement updateCategory = conn
					.prepareStatement("UPDATE FINDINGS_OVERVIEW SET CATEGORY = ? WHERE FINDING_TYPE = ?");
			Statement st = conn.createStatement();
			try {
				ResultSet set = st
						.executeQuery("SELECT C.NAME,FT.NAME"
								+ "   FROM FINDING_TYPE FT, CATEGORY_FINDING_TYPE_RELTN CFR,FINDING_CATEGORY C"
								+ "   WHERE CFR.FINDING_TYPE_ID = FT.ID AND C.ID = CFR.CATEGORY_ID");
				while (set.next()) {
					int idx = 1;
					updateCategory.setString(idx++, set.getString(1));
					updateCategory.setString(idx++, set.getString(2));
					updateCategory.execute();
				}
			} finally {
				st.close();
			}
		}
	}

}
