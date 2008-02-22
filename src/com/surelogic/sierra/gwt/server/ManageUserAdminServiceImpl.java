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

	public String createUser(final UserAccount account, final String password) {
		if ((account == null) || (password == null)
				|| (account.getUserName() == null)
				|| (account.getUserName().length() == 0)) {
			return "Invalid arguments";
		} else {
			return performAdmin(false, new UserTransaction<String>() {

				public String perform(Connection conn, Server server,
						User serverUser) throws SQLException {
					final ServerUserManager man = ServerUserManager
							.getInstance(conn);
					final String user = account.getUserName();
					if (man.createUser(user, password)) {
						if (account.isAdministrator()) {
							man.addUserToGroup(user, SierraGroup.ADMIN);
						}
						return user + " created.";
					} else {
						return "Could not create user with name " + user + ".";
					}
				}
			});
		}
	}

	public List<UserAccount> findUser(final String userQueryString) {
		return performAdmin(true, new UserTransaction<List<UserAccount>>() {

			public List<UserAccount> perform(Connection conn, Server server,
					User user) throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);

				final List<User> users = man.findUser(userQueryString);
				final List<UserAccount> userAccounts = new ArrayList<UserAccount>(
						users.size());
				for (User u : users) {
					userAccounts.add(convertUser(man, u));
				}
				return userAccounts;
			}

		});
	}

	public List<UserAccount> getUsers() {
		return performAdmin(true, new UserTransaction<List<UserAccount>>() {

			public List<UserAccount> perform(Connection conn, Server server,
					User user) throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);

				final List<User> users = man.listUsers();
				final List<UserAccount> userAccounts = new ArrayList<UserAccount>(
						users.size());
				for (User u : users) {
					userAccounts.add(convertUser(man, u));
				}
				return userAccounts;
			}
		});
	}

	public UserAccount getUserInfo(final String targetUser) {
		return performAdmin(true, new UserTransaction<UserAccount>() {

			public UserAccount perform(Connection conn, Server server,
					User serverUser) throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				List<User> users = man.findUser(targetUser);
				for (User user : users) {
					if (user.getName().equals(targetUser)) {
						return convertUser(man, user);
					}
				}
				return null;
			}
		});
	}

	public UserAccount updateUser(final UserAccount account,
			final String password) {
		return performAdmin(false, new UserTransaction<UserAccount>() {

			public UserAccount perform(Connection conn, Server server, User user)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				final String targetUserName = account.getUserName();
				man.changeUserName(account.getId(), account.getUserName());
				if (password != null) {
					man.changeUserPassword(targetUserName, password);
				}
				if (account.isAdministrator()) {
					man.addUserToGroup(targetUserName, SierraGroup.ADMIN);
				} else {
					man.removeUserFromGroup(targetUserName, SierraGroup.ADMIN);
				}
				return account;
			}
		});
	}

	public boolean deleteUser(final String targetUser) {
		performAdmin(false, new UserTransaction<Boolean>() {

			public Boolean perform(Connection conn, Server server, User user)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				man.deleteUser(targetUser);
				return null;
			}
		});
		// FIXME assume the user was deleted for now
		return true;
	}

	public boolean isAvailable() {
		Boolean isAdmin = performAdmin(true, new UserTransaction<Boolean>() {
			public Boolean perform(Connection conn, Server server, User user)
					throws Exception {
				return Boolean.TRUE;
			}
		});
		return Boolean.TRUE.equals(isAdmin);
	}

	private <T> T performAdmin(boolean readOnly, final UserTransaction<T> t) {
		UserTransaction<T> adminTrans = new UserTransaction<T>() {

			public T perform(Connection conn, Server server, User user)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				if (man.isUserInGroup(user.getName(), SierraGroup.ADMIN
						.getName())) {
					return t.perform(conn, server, user);
				} else {
					return null;
				}
			}
		};
		if (readOnly) {
			return ConnectionFactory.withUserReadOnly(adminTrans);
		}
		return ConnectionFactory.withUserTransaction(adminTrans);
	}

	private UserAccount convertUser(ServerUserManager man, User u)
			throws SQLException {
		final String userName = u.getName();
		boolean isAdmin = man.isUserInGroup(userName, SierraGroup.ADMIN
				.getName());
		return new UserAccount(u.getId(), userName, isAdmin);
	}
}
