package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class User {

	private Long id;

	public static User getUser(Connection conn) throws SQLException {
		User user = new User();
		java.sql.Statement st = conn.createStatement();
		try {
			ResultSet set = st
					.executeQuery("SELECT ID FROM SIERRA_USER WHERE USER_NAME='eclipse'");
			if (!set.next()) {
				st
						.executeUpdate(
								"INSERT INTO SIERRA_USER (USER_NAME) VALUES ('eclipse')",
								Statement.RETURN_GENERATED_KEYS);
				set = st.getGeneratedKeys();
				set.next();
			}
			user.id = set.getLong(1);
		} finally {
			st.close();
		}
		return user;
	}

	public Long getId() {
		return id;
	}

	public static User findOrCreate(Connection conn, String user)
			throws SQLException {
		PreparedStatement st = conn
				.prepareStatement("SELECT ID FROM SIERRA_USER WHERE USER_NAME = ?");
		st.setString(1, user);
		ResultSet set = st.executeQuery();
		if (!set.next()) {
			st = conn.prepareStatement(
					"INSERT INTO SIERRA_USER (USER_NAME) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, user);
			st.executeUpdate();
			set = st.getGeneratedKeys();
		}
		set.next();
		User ret = new User();
		ret.id = set.getLong(1);
		return ret;
	}
}
