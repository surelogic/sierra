package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ManageUserAdminService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
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
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<String>() {

					public String perform(Connection conn, Server server,
							User serverUser) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(serverUser.getName(),
								SierraGroup.ADMIN.getName())) {
							if (man.createUser(user, password)) {
								return user + " created.";
							} else {
								return "Could not create user with name "
										+ user + ".";
							}
						} else {
							// No permissions
							return null;
						}
					}
				});
	}

	public List<UserAccount> findUser(final String userQueryString) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<UserAccount>>() {

					public List<UserAccount> perform(Connection conn,
							Server server, User user) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(user.getName(), SierraGroup.ADMIN
								.getName())) {
							final List<User> users = man
									.findUser(userQueryString);
							final List<UserAccount> userAccounts = new ArrayList<UserAccount>(
									users.size());
							for (User u : users) {
								userAccounts.add(convertUser(man, u));
							}
							return userAccounts;
						} else {
							return null;
						}
					}
				});
	}

	public List<UserAccount> getUsers() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<UserAccount>>() {

					public List<UserAccount> perform(Connection conn,
							Server server, User user) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(user.getName(), SierraGroup.ADMIN
								.getName())) {
							final List<User> users = man.listUsers();
							final List<UserAccount> userAccounts = new ArrayList<UserAccount>(
									users.size());
							for (User u : users) {
								userAccounts.add(convertUser(man, u));
							}
							return userAccounts;
						} else {
							return null;
						}
					}
				});
	}

	public UserAccount getUserInfo(final String user) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<UserAccount>() {

					public UserAccount perform(Connection conn, Server server,
							User serverUser) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(serverUser.getName(),
								SierraGroup.ADMIN.getName())) {
							return new UserAccount(user, man.isUserInGroup(
									user, SierraGroup.ADMIN.getName()));
						} else {
							return null;
						}
					}
				});
	}

	public UserAccount updateUser(final String user, final String password,
			final boolean isAdmin) {
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<UserAccount>() {

					public UserAccount perform(Connection conn, Server server,
							User serverUser) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (man.isUserInGroup(serverUser.getName(),
								SierraGroup.ADMIN.getName())) {
							if (password != null) {
								man.changeUserPassword(user, password);
							}
							if (isAdmin) {
								man.addUserToGroup(user, SierraGroup.ADMIN
										.getName());
							} else {
								man.removeUserFromGroup(user, SierraGroup.ADMIN
										.getName());
							}
							return new UserAccount(user, man.isUserInGroup(
									user, SierraGroup.ADMIN.getName()));
						} else {
							return null;
						}
					}
				});
	}

	public void deleteUser(final String user) {
		ConnectionFactory.withUserTransaction(new UserTransaction<Void>() {

			public Void perform(Connection conn, Server server, User serverUser)
					throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(serverUser.getName(), SierraGroup.ADMIN
						.getName())) {
					man.deleteUser(user);
				}
				return null;
			}
		});
	}

	public boolean isAvailable() {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<Boolean>() {

					public Boolean perform(Connection conn, Server server,
							User user) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						return man.isUserInGroup(user.getName(),
								SierraGroup.ADMIN.getName());
					}
				});
	}

	private UserAccount convertUser(ServerUserManager man, User u)
			throws SQLException {
		final String userName = u.getName();
		boolean isAdmin = man.isUserInGroup(userName, SierraGroup.ADMIN
				.getName());
		return new UserAccount(userName, isAdmin);
	}
}
