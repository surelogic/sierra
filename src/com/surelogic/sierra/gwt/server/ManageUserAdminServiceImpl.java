package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.ManageUserAdminService;
import com.surelogic.sierra.gwt.client.UserInfo;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.SierraGroup;
import com.surelogic.sierra.jdbc.user.User;

public class ManageUserAdminServiceImpl extends SierraServiceServlet implements
		ManageUserAdminService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 946194129762715684L;

	public String createUser(final String user, final String password) {
		return ServerConnection.withTransaction(new WebTransaction<String>() {

			public String perform(Connection conn, Server server)
					throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
						.getName())) {
					if (man.createUser(user, password)) {
						return user + " created.";
					} else {
						return "Could not create user with name " + user + ".";
					}
				} else {
					// No permissions
					return null;
				}
			}
		});
	}

	public List<String> findUser(final String userQueryString) {
		return ServerConnection
				.withReadOnly(new WebTransaction<List<String>>() {

					public List<String> perform(Connection conn, Server server)
							throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
								.getName())) {
							final List<User> users = man
									.findUser(userQueryString);
							final List<String> userNames = new ArrayList<String>(
									users.size());
							for (User u : users) {
								userNames.add(u.getName());
							}
							return userNames;
						} else {
							return null;
						}
					}
				});
	}

	public List<String> getUsers() {
		return ServerConnection
				.withReadOnly(new WebTransaction<List<String>>() {

					public List<String> perform(Connection conn, Server server)
							throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
								.getName())) {
							final List<User> users = man.listUsers();
							final List<String> userNames = new ArrayList<String>(
									users.size());
							for (User u : users) {
								userNames.add(u.getName());
							}
							return userNames;
						} else {
							return null;
						}
					}
				});
	}

	public UserInfo getUserInfo(final String user) {
		return ServerConnection.withReadOnly(new WebTransaction<UserInfo>() {

			public UserInfo perform(Connection conn, Server server)
					throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
						.getName())) {
					return new UserInfo(user, man.isUserInGroup(user,
							SierraGroup.ADMIN.getName()));
				} else {
					return null;
				}
			}
		});
	}

	public UserInfo updateUser(final String user, final String password,
			final boolean isAdmin) {
		return ServerConnection.withTransaction(new WebTransaction<UserInfo>() {

			public UserInfo perform(Connection conn, Server server)
					throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
						.getName())) {
					if (password != null) {
						man.changeUserPassword(user, password);
					}
					if (isAdmin) {
						man.addUserToGroup(user, SierraGroup.ADMIN.getName());
					} else {
						man.removeUserFromGroup(user, SierraGroup.ADMIN.getName());
					}
					return new UserInfo(user, man.isUserInGroup(user,
							SierraGroup.ADMIN.getName()));
				} else {
					return null;
				}
			}
		});
	}

	public void deleteUser(final String user) {
		ServerConnection.withTransaction(new WebTransaction<Void>() {

			public Void perform(Connection conn, Server server)
					throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(getUserName(), SierraGroup.ADMIN
						.getName())) {
					man.deleteUser(user);
				}
				return null;
			}
		});
	}

	public boolean isAvailable() {
		return ServerConnection.withReadOnly(new WebTransaction<Boolean>() {

			public Boolean perform(Connection conn, Server server)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				return man.isUserInGroup(getUserName(), SierraGroup.ADMIN.getName());
			}
		});
	}

}
