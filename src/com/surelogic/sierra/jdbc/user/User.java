package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class User {

	private Long id;

	public static User getUser(String userName, Connection conn)
			throws SQLException {
		User user = new User();
		PreparedStatement st = conn
				.prepareStatement("SELECT ID FROM SIERRA_USER WHERE USER_NAME = ?");
		st.setString(1, userName);
		ResultSet set = st.executeQuery();
		if (!set.next()) {
			st = conn.prepareStatement(
					"INSERT INTO SIERRA_USER (USER_NAME) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, userName);
			st.executeUpdate();
			set = st.getGeneratedKeys();
			set.next();
		}
		user.id = set.getLong(1);
		return user;
	}

	public Long getId() {
		return id;
	}
}
