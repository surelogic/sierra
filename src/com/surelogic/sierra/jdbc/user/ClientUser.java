package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.surelogic.sierra.jdbc.DBType;
import com.surelogic.sierra.jdbc.JDBCUtils;

public final class ClientUser implements User {

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

	public String getName() {
		return userName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ClientUser other = (ClientUser) obj;
		if (id != other.id)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
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
