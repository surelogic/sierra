package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.record.UpdateBaseMapper;
import com.surelogic.sierra.jdbc.record.UpdateRecordMapper;
import com.surelogic.sierra.jdbc.record.UserRecord;

public class ServerUserManager {

	private final UpdateRecordMapper userMapper;

	private ServerUserManager(Connection conn) throws SQLException {
		userMapper = new UpdateBaseMapper(conn,
				"INSERT INTO SIERRA_USER (USER_NAME,SALT,HASH) VALUES (?,?,?)",
				"SELECT ID, SALT, HASH FROM SIERRA_USER WHERE USER_NAME = ?",
				"DELETE FROM SIERRA_USER WHERE ID = ?",
				"UPDATE SIERRA_USER SET SALT = ?, HASH = ? WHERE ID = ?");
	}

	public static ServerUserManager getInstance(Connection conn)
			throws SQLException {
		return new ServerUserManager(conn);
	}

	public User login(String userName, String password) throws SQLException {
		final UserRecord record = getUser(userName);
		if ((record != null) && record.getPassword().check(password)) {
			return new ServerUser(record.getId(), record.getUserName());
		} else {
			return null;
		}
	}

	public User createUser(String userName, String password) throws SQLException {
		final UserRecord record = new UserRecord(userMapper);
		record.setUserName(userName);
		if(record.select()) {
			throw new IllegalArgumentException("User with name " + userName + " already exists");
		} else {
			record.setPassword(Password.newPassword(password));
			record.insert();
			return new ServerUser(record.getId(), record.getUserName());
		}
	}

	private UserRecord getUser(String userName) throws SQLException {
		UserRecord rec = new UserRecord(userMapper);
		rec.setUserName(userName);
		if (rec.select()) {
			return rec;
		} else {
			return null;
		}
	}

	private static class ServerUser implements User {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8894109172112328940L;

		private final long id;
		private final String userName;

		ServerUser(long id, String userName) {
			this.id = id;
			this.userName = userName;
		}

		public long getId() {
			return id;
		}

		public String getUserName() {
			return userName;
		}

	}
}
