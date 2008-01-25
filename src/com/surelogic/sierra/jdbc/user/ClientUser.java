package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

public class ClientUser implements User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5440911229476307762L;	
	
	private final long id;
	private final String userName;

	private ClientUser(long id, String userName) {
		this.id = id;
		this.userName = userName;
	}

	public long getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public static User getUser(String userName, Connection conn)
			throws SQLException {
		PreparedStatement st = conn
				.prepareStatement("SELECT ID FROM SIERRA_USER WHERE USER_NAME = ?");
		st.setString(1, userName);
		ResultSet set = st.executeQuery();
		try {
			if (!set.next()) {
				set.close();
				if (DBType.ORACLE == JDBCUtils.getDb(conn)) {
					st = conn.prepareStatement(
							"INSERT INTO SIERRA_USER (USER_NAME) VALUES (?)",
							new String[] { "ID" });
				} else {
					st = conn.prepareStatement(
							"INSERT INTO SIERRA_USER (USER_NAME) VALUES (?)",
							Statement.RETURN_GENERATED_KEYS);
				}
				st.setString(1, userName);
				st.executeUpdate();
				set = st.getGeneratedKeys();
				set.next();
			}
			return new ClientUser(set.getLong(1), userName);
		} finally {
			set.close();
		}
	}
}
