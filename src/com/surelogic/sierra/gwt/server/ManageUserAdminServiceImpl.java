package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Result;
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

	public Result<String> createUser(final UserAccount account,
			final String password) {
		if ((account == null) || (password == null)
				|| (account.getUserName() == null)
				|| (account.getUserName().length() == 0)) {
			return Result.failure("Missing required field.");
		} else {
			return performAdmin(false, new UserTransaction<Result<String>>() {

				public Result<String> perform(final Connection conn,
						final Server server, final User serverUser)
						throws SQLException {
					final ServerUserManager man = ServerUserManager
							.getInstance(conn);
					final String user = account.getUserName();
					if (man.createUser(user, password)) {
						if (account.isAdministrator()) {
							man.addUserToGroup(user, SierraGroup.ADMIN);
						}
						return Result.success(user + " created.");
					} else {
						return Result
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

			public List<UserAccount> perform(final Connection conn,
					final Server server, final User user) throws SQLException {
				final ServerUserManager man = ServerUserManager
						.getInstance(conn);

				final List<User> users = man.findUser(userQueryString);
				final List<UserAccount> userAccounts = new ArrayList<UserAccount>(
						users.size());
				for (final User u : users) {
					userAccounts.add(convertUser(man, u));
				}
				return userAccounts;
			}

		});
	}

	public List<UserAccount> getUsers() {
		return ConnectionFactory.getInstance().withUserReadOnly(
				new UserTransaction<List<UserAccount>>() {

					public List<UserAccount> perform(final Connection conn,
							final Server server, final User user)
							throws SQLException {
						return listUsers(conn);
					}
				});
	}

	public UserAccount getUserInfo(final String targetUser) {
		return performAdmin(true, new UserTransaction<UserAccount>() {

			public UserAccount perform(final Connection conn,
					final Server server, final User serverUser)
					throws SQLException {
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

	public Result<String> changeUserPassword(final String targetUser,
			final String currentUserPassword, final String newPassword) {
		return ConnectionFactory.getInstance().withUserTransaction(
				new UserTransaction<Result<String>>() {

					public Result<String> perform(final Connection conn,
							final Server server, final User user)
							throws Exception {
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

	public Result<UserAccount> updateUser(final UserAccount account) {
		return performAdmin(false, new UserTransaction<Result<UserAccount>>() {

			public Result<UserAccount> perform(final Connection conn,
					final Server server, final User user) throws Exception {
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
					for (final UserAccount u : listUsers(conn)) {
						if (u.isActive() && u.isAdministrator()) {
							count++;
						}
					}
					if (count < 2) {
						return Result
								.failure(
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
		final Boolean isAdmin = performAdmin(true,
				new UserTransaction<Boolean>() {
					public Boolean perform(final Connection conn,
							final Server server, final User user)
							throws Exception {
						return Boolean.TRUE;
					}
				});
		return Boolean.TRUE.equals(isAdmin);
	}

	private List<UserAccount> listUsers(final Connection conn)
			throws SQLException {
		final ServerUserManager man = ServerUserManager.getInstance(conn);

		final List<User> users = man.listUsers();
		final List<UserAccount> userAccounts = new ArrayList<UserAccount>(users
				.size());
		for (final User u : users) {
			userAccounts.add(convertUser(man, u));
		}
		return userAccounts;
	}

	private UserAccount convertUser(final ServerUserManager man, final User u)
			throws SQLException {
		final String userName = u.getName();
		final boolean isAdmin = man.isUserInGroup(userName, SierraGroup.ADMIN
				.getName());
		return new UserAccount(u.getId(), userName, isAdmin, u.isActive());
	}

	public void updateUsersStatus(final List<String> users,
			final boolean isActive) {
		if (users != null && !users.isEmpty()) {
			performAdmin(false, new UserTransaction<Void>() {

				public Void perform(final Connection conn, final Server server,
						final User user) throws Exception {
					final ServerUserManager man = ServerUserManager
							.getInstance(conn);
					for (final Object userName : users) {
						man.changeUserStatus((String) userName, isActive);
					}
					return null;
				}
			});
		}
	}
}
