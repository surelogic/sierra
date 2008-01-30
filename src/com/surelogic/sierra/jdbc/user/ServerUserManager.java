package com.surelogic.sierra.jdbc.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.record.GroupRecord;
import com.surelogic.sierra.jdbc.record.UserGroupRelationRecord;
import com.surelogic.sierra.jdbc.record.UserRecord;
import com.surelogic.sierra.jdbc.record.RelationRecord.PK;

/**
 * ServerUserManager is responsible for managing server's user accounts.
 * 
 * @author nathan
 * 
 */
public class ServerUserManager {

	private final UserRecordFactory factory;
	private final PreparedStatement selectUsers;
	private final PreparedStatement selectSomeUsers;

	private ServerUserManager(Connection conn) throws SQLException {
		this.factory = UserRecordFactory.getInstance(conn);
		selectUsers = conn
				.prepareStatement("SELECT ID, USER_NAME FROM SIERRA_USER");
		selectSomeUsers = conn
				.prepareStatement("SELECT ID, USER_NAME FROM SIERRA_USER WHERE USER_NAME LIKE ?");
	}

	public static ServerUserManager getInstance(Connection conn)
			throws SQLException {
		return new ServerUserManager(conn);
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return a valid User object if the login is successful, <code>null</code>
	 *         if not
	 * @throws SQLException
	 */
	public User login(String userName, String password) throws SQLException {
		final UserRecord record = getUser(userName);
		if ((record != null) && record.getPassword().check(password)) {
			return new ServerUser(record.getId(), record.getUserName());
		} else {
			return null;
		}
	}

	public boolean isUserInGroup(String user, String group) throws SQLException {
		final UserGroupRelationRecord record = factory.newGroupUser();
		record.setId(new PK<Long, Long>(getUser(user).getId(), getGroup(group)
				.getId()));
		return record.select();
	}

	public boolean addUserToGroup(String user, String group)
			throws SQLException {
		final UserGroupRelationRecord record = factory.newGroupUser();
		record.setId(new PK<Long, Long>(getUser(user).getId(), getGroup(group)
				.getId()));
		final boolean exists = record.select();
		if (!exists) {
			record.insert();
		}
		return exists;
	}

	/**
	 * Create a new user with the given name and password.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public boolean createUser(String userName, String password)
			throws SQLException {
		final UserRecord record = factory.newUser();
		record.setUserName(userName);
		if (!record.select()) {
			record.setPassword(Password.newPassword(password));
			record.insert();
			addUserToGroup(userName, SierraGroup.USER.getName());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Create a new group with the given name and and description
	 * 
	 * @param name
	 * @param description
	 *            may be <code>null</code>
	 * @return
	 * @throws SQLException
	 */
	public boolean createGroup(String name, String description)
			throws SQLException {
		final GroupRecord group = factory.newGroup();
		group.setName(name);
		if (!group.select()) {
			group.setInfo(description);
			group.insert();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Changes the user's password, but only if the old password is valid.
	 * 
	 * @param userName
	 * @param oldPassword
	 * @param newPassword
	 * @return <code>true</code> if the password has been changed,
	 *         <code>false</code> otherwise.
	 * @throws SQLException
	 */
	public boolean changeUserPassword(String userName, String oldPassword,
			String newPassword) throws SQLException {
		final UserRecord record = getUser(userName);
		if (record.getPassword().check(oldPassword)) {
			final Password password = Password.newPassword(newPassword);
			record.setPassword(password);
			record.update();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return a list of users.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<User> listUsers() throws SQLException {
		final ResultSet set = selectUsers.executeQuery();
		try {
			final List<User> list = new ArrayList<User>();
			while (set.next()) {
				list.add(new ServerUser(set.getLong(1), set.getString(2)));
			}
			return list;
		} finally {
			set.close();
		}
	}

	/**
	 * Find users that contain the given query string. <code>*</code> may be
	 * used to represent a wildcard.
	 * 
	 * @param userQueryString
	 * @return
	 * @throws SQLException
	 */
	public List<User> findUser(String userQueryString) throws SQLException {
		selectSomeUsers.setString(1, userQueryString.replace('*', '%').concat(
				"%"));
		final ResultSet set = selectSomeUsers.executeQuery();
		try {
			final List<User> list = new ArrayList<User>();
			while (set.next()) {
				list.add(new ServerUser(set.getLong(1), set.getString(2)));
			}
			return list;
		} finally {
			set.close();
		}
	}

	private UserRecord getUser(String userName) throws SQLException {
		UserRecord rec = factory.newUser();
		rec.setUserName(userName);
		if (rec.select()) {
			return rec;
		} else {
			return null;
		}
	}

	private GroupRecord getGroup(String groupName) throws SQLException {
		GroupRecord rec = factory.newGroup();
		rec.setName(groupName);
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
			final ServerUser other = (ServerUser) obj;
			if (id != other.id)
				return false;
			if (userName == null) {
				if (other.userName != null)
					return false;
			} else if (!userName.equals(other.userName))
				return false;
			return true;
		}

	}
}
