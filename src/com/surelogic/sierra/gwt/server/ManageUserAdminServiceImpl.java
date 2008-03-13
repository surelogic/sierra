package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.Status;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.ManageUserAdminService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.SecurityHelper;
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

	public Status createUser(final UserAccount account, final String password) {
		if ((account == null) || (password == null)
				|| (account.getUserName() == null)
				|| (account.getUserName().length() == 0)) {
			return Status.failure("Invalid arguments");
		} else {
			return performAdmin(false, new UserTransaction<Status>() {

				public Status perform(Connection conn, Server server,
						User serverUser) throws SQLException {
					final ServerUserManager man = ServerUserManager
							.getInstance(conn);
					final String user = account.getUserName();
					if (man.createUser(user, password)) {
						if (account.isAdministrator()) {
							man.addUserToGroup(user, SierraGroup.ADMIN);
						}
						return Status.success(user + " created.");
					} else {
						return Status
								.failure("Could not create user with name "
										+ user
										+ ".  A user with that name already exists.");
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
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<List<UserAccount>>() {

					public List<UserAccount> perform(Connection conn,
							Server server, User user) throws SQLException {
						return listUsers(conn);
					}
				});
	}

	public UserAccount getUserInfo(final String targetUser) {
		return performAdmin(true, new UserTransaction<UserAccount>() {

			public UserAccount perform(Connection conn, Server server,
					User serverUser) throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				final User user = man.getUserByName(targetUser);
				if (user != null) {
					return convertUser(man, user);
				}
				return null;
			}
		});
	}

	public Result changeUserPassword(final String targetUser,
			final String currentUserPassword, final String newPassword) {
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<Result>() {

					public Result perform(Connection conn, Server server,
							User user) throws Exception {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						if (user.getName().equals(targetUser)
								|| man.isUserInGroup(user.getName(),
										SierraGroup.ADMIN.getName())) {
							if (man.login(user.getName(), currentUserPassword) != null) {
								man.changeUserPassword(targetUser, newPassword);
								return Result
										.success("Password changed successfully.");
							} else {
								return Result
										.failure("Invalid password. Please enter your current password correctly.");
							}
						} else {
							return Result
									.failure("You do not have permissions to change this user's password.");
						}
					}
				});
	}

	public Result updateUser(final UserAccount account) {
		return performAdmin(false, new UserTransaction<Result>() {

			public Result perform(Connection conn, Server server, User user)
					throws Exception {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);
				final String targetUserName = account.getUserName();
				man.changeUserName(account.getId(), account.getUserName());
				final User targetUser = man.getUserByName(targetUserName);
				final UserAccount targetUserAccount = convertUser(man, man
						.getUserByName(targetUserName));
				// Make sure that the user name of the session stays valid
				if (targetUser.getId() == user.getId()) {
					getThreadLocalRequest().getSession().setAttribute(
							SecurityHelper.USER, targetUser);
				}
				if (targetUser.isActive()
						&& targetUserAccount.isAdministrator()
						&& (!account.isActive() || !account.isAdministrator())) {
					int count = 0;
					for (UserAccount u : listUsers(conn)) {
						if (u.isActive() && u.isAdministrator()) {
							count++;
						}
					}
					if (count < 2) {
						return Result
								.fail(
										"The server must always have at least one active administrator.",
										targetUserAccount);
					}
				}
				man.changeUserStatus(targetUserName, account.isActive());
				if (account.isAdministrator()) {
					man.addUserToGroup(targetUserName, SierraGroup.ADMIN);
				} else {
					man.removeUserFromGroup(targetUserName, SierraGroup.ADMIN);
				}
				return Result.success(account.getUserName() + " updated.",
						account);
			}
		});
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

	private List<UserAccount> listUsers(Connection conn) throws SQLException {
		final ServerUserManager man = ServerUserManager.getInstance(conn);

		final List<User> users = man.listUsers();
		final List<UserAccount> userAccounts = new ArrayList<UserAccount>(users
				.size());
		for (User u : users) {
			userAccounts.add(convertUser(man, u));
		}
		return userAccounts;
	}

	private UserAccount convertUser(ServerUserManager man, User u)
			throws SQLException {
		final String userName = u.getName();
		boolean isAdmin = man.isUserInGroup(userName, SierraGroup.ADMIN
				.getName());
		return new UserAccount(u.getId(), userName, isAdmin, u.isActive());
	}

	@SuppressWarnings("unchecked")
	public void updateUsersStatus(final List users, final boolean isActive) {
		if (users != null && !users.isEmpty()) {
			performAdmin(false, new UserTransaction<Void>() {

				public Void perform(Connection conn, Server server, User user)
						throws Exception {
					final ServerUserManager man = ServerUserManager
							.getInstance(conn);
					for (Object userName : users) {
						man.changeUserStatus((String) userName, isActive);
					}
					return null;
				}
			});
		}
	}
}
