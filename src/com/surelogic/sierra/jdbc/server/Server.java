package com.surelogic.sierra.jdbc.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class Server {

	public static User getUser(String userName, Connection conn)
			throws SQLException {
		User user = new User();
		PreparedStatement st = conn
				.prepareStatement("SELECT ID FROM SIERRA_USER WHERE USER_NAME = ?");
		st.setString(1, userName);
		ResultSet set = st.executeQuery();
		try {
			if (!set.next()) {
				set.close();
				st = conn.prepareStatement(
						"INSERT INTO SIERRA_USER (USER_NAME) VALUES (?)",
						Statement.RETURN_GENERATED_KEYS);
				st.setString(1, userName);
				st.executeUpdate();
				set = st.getGeneratedKeys();
				set.next();
			}
			user.id = set.getLong(1);
		} finally {
			set.close();
		}
		return user;
	}

	public static Long nextRevision(Connection conn) throws SQLException {
		PreparedStatement st = conn.prepareStatement(
				"INSERT INTO REVISION (DATE_TIME) VALUES (?)",
				Statement.RETURN_GENERATED_KEYS);
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
				"SELECT UID FROM SERVER");
		try {
			set.next();
			return set.getString(1);
		} finally {
			set.close();
		}

	}

}
