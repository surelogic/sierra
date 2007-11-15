package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

/**
 * I am replacing this with ServerConnection, I think
 */
public class Server {

	public static Long nextRevision(Connection conn) throws SQLException {
		PreparedStatement st;
		if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
			st = conn.prepareStatement(
					"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
					new String[] { "REVISION" });
		} else {
			st = conn.prepareStatement(
					"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS);
		}
		st.setTimestamp(1, new Timestamp(new Date().getTime()));
		st.execute();
		ResultSet set = st.getGeneratedKeys();
		try {
			set.next();
			return set.getLong(1);
		} finally {
			set.close();
		}

	}

	public static String getUid(Connection conn) throws SQLException {
		ResultSet set = conn.createStatement().executeQuery(
				"SELECT UUID FROM SERVER");
		try {
			set.next();
			return set.getString(1);
		} finally {
			set.close();
		}

	}

}
