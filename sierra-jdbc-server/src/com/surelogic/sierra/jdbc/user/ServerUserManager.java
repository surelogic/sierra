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
public final class ServerUserManager {

	private final UserRecordFactory factory;
	private final PreparedStatement selectUsers;
	private final PreparedStatement selectSomeUsers;
	private final PreparedStatement updateUserName;

	private ServerUserManager(Connection conn) throws SQLException {
		this.factory = UserRecordFactory.getInstance(conn);
		selectUsers = conn
				.prepareStatement("SELECT ID, USER_NAME, IS_ACTIVE FROM SIERRA_USER");
		selectSomeUsers = conn
				.prepareStatement("SELECT ID, USER_NAME, IS_ACTIVE FROM SIERRA_USER WHERE USER_NAME LIKE ?");
		updateUserName = conn
				.prepareStatement("UPDATE SIERRA_USER SET USER_NAME = ? WHERE ID = ?");
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
		if ((record != null) && record.isActive()
				&& record.getPassword().check(password)) {
			return new ServerUser(record.getId(), record.getUserName(), record
					.isActive());
		} else {
			return null;
		}
	}

	/**
	 * Checks to see whether or not a user has access to a particular group.
	 * 
	 * @param user
	 * @param group
	 * @return
	 * @throws SQLException
	 */
	public boolean isUserInGroup(String user, String group) throws SQLException {
		final UserGroupRelationRecord record = factory.newGroupUser();
		record.setId(new PK<Long, Long>(getUser(user).getId(), getGroup(group)
				.getId()));
		return record.select();
	}

	/**
	 * Adds a user to the given group. If the user is already part of the group,
	 * does nothing.
	 * 
	 * @param user
	 * @param group
	 * @throws SQLException
	 */
	public void addUserToGroup(String user, SierraGroup group)
			throws SQLException {
		final UserGroupRelationRecord record = factory.newGroupUser();
		record.setId(new PK<Long, Long>(getUser(user).getId(), getGroup(
				group.getName()).getId()));
		final boolean exists = record.select();
		if (!exists) {
			record.insert();
		}
	}

	/**
	 * Remove a user from the given group. If the user is not part of the group,
	 * does nothing.
	 * 
	 * @param user
	 * @param sierraGroup
	 * @throws SQLException
	 */
	public void removeUserFromGroup(String user, SierraGroup sierraGroup)
			throws SQLException {
		final UserGroupRelationRecord record = factory.newGroupUser();
		record.setId(new PK<Long, Long>(getUser(user).getId(), getGroup(
				sierraGroup.getName()).getId()));
		final boolean exists = record.select();
		if (exists) {
			record.delete();
		}
	}

	/**
	 * Create a new user with the given name and password.
	 * 
	 * @param userName
	 * @param password
	 * @return -{@code true} if the user is created {@code false} if the user
	 *         already exists
	 * @throws SQLException
	 */
	public boolean createUser(String userName, String password)
			throws SQLException {
		final UserRecord record = factory.newUser();
		record.setUserName(userName);
		if (!record.select()) {
			record.setPassword(Password.newPassword(password));
			record.setActive(true);
			record.insert();
			addUserToGroup(userName, SierraGroup.USER);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Disable an existing user.
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void changeUserStatus(String user, boolean isActive)
			throws SQLException {
		final UserRecord record = factory.newUser();
		record.setUserName(user);
		if (record.select()) {
			if (!(isActive == record.isActive())) {
				record.setActive(isActive);
				record.update();
			}
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
	 * Change the given user's name. If the user with the given id already has
	 * that user name, does nothing.
	 * 
	 * @param id
	 * @param newUserName
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 *             if another user already has that name
	 */
	public void changeUserName(long id, String newUserName) throws SQLException {
		if (newUserName != null && newUserName.length() != 0) {
			final UserRecord user = getUser(newUserName);
			if (user == null) {
				updateUserName.setString(1, newUserName);
				updateUserName.setLong(2, id);
				updateUserName.execute();
			} else if (!newUserName.equals(user.getUserName())) {
				throw new IllegalArgumentException("User name already taken.");
			}
		} else {
			throw new IllegalArgumentException("Invalid  new user name.");
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
	 * Changes the user's password. This does not check the user's current
	 * password, and should only be used by someone in an administrative role.
	 * 
	 * @param userName
	 * @param newPassword
	 * @return <code>true</code> if the password has been changed,
	 *         <code>false</code> otherwise.
	 * @throws SQLException
	 */
	public void changeUserPassword(String userName, String newPassword)
			throws SQLException {
		final UserRecord record = getUser(userName);
		final Password password = Password.newPassword(newPassword);
		record.setPassword(password);
		record.update();
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
				list.add(new ServerUser(set.getLong(1), set.getString(2), "Y"
						.equals(set.getString(3))));
			}
			return list;
		} finally {
			set.close();
		}
	}

	public User getUserByName(String name) throws SQLException {
		final UserRecord record = getUser(name);
		if (record != null) {
			return new ServerUser(record.getId(), record.getUserName(), record
					.isActive());
		} else {
			return null;
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
				list.add(new ServerUser(set.getLong(1), set.getString(2), "Y"
						.equals(set.getString(3))));
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

	public static ServerUserManager getInstance(Connection conn)
			throws SQLException {
		return new ServerUserManager(conn);
	}

	private static class ServerUser implements User {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8894109172112328940L;

		private final long id;
		private final String userName;
		private final boolean isActive;

		ServerUser(long id, String userName, boolean isActive) {
			this.id = id;
			this.userName = userName;
			this.isActive = isActive;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return userName;
		}

		public boolean isActive() {
			return isActive;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
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
			return true;
		}

	}

}
