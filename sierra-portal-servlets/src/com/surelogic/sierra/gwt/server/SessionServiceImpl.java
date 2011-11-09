package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.Result;
import com.surelogic.sierra.gwt.client.data.ServerType;
import com.surelogic.sierra.gwt.client.data.UserAccount;
import com.surelogic.sierra.gwt.client.service.SessionService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.SecurityHelper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.SierraGroup;
import com.surelogic.sierra.jdbc.user.User;

public class SessionServiceImpl extends SierraServiceServlet implements
		SessionService {
	private static final long serialVersionUID = -6665683550116264375L;
	private static final Logger log = SLLogger
			.getLoggerFor(SessionServiceImpl.class);

	public Result<UserAccount> getUserAccount() {
		final User u = UserContext.peek();
		if (u == null) {
			return Result.failure("", null);
		}

		return Result.success(getUserAccount(u));
	}

	public Result<UserAccount> login(final String userName,
			final String password) {
		if ((userName != null) && (password != null)) {
			final User u = ConnectionFactory.getInstance().withReadOnly(
					new ServerTransaction<User>() {

						public User perform(final Connection conn,
								final Server server) throws SQLException {
							return ServerUserManager.getInstance(conn).login(
									userName, password);
						}
					});
			if (u != null) {
				getThreadLocalRequest().getSession().setAttribute(
						SecurityHelper.USER, u);
				UserContext.set(u);
				log.info("User " + userName + " logged in successfully.");
				return Result.success(getUserAccount(u));
			} else {
				log.info("Failed logging attempt for user " + userName + ".");
				return Result.failure("Invalid username or password", null);
			}
		} else {
			return Result.failure("Missing username or password", null);
		}
	}

	public Result<String> logout() {
		// TODO change this to return a Status instead
		try {
			getThreadLocalRequest().getSession().invalidate();
		} catch (final IllegalStateException ise) {
			// ignore exception if already logged out
		}
		UserContext.set(null);
		return Result.success("Logged out.", null);
	}

	public ServerType getServerType() {
		final String teamserver = getServletContext().getInitParameter(
				"teamserver");
		return ((teamserver == null) || !"on".equals(teamserver)) ? ServerType.BUGLINK
				: ServerType.TEAMSERVER;
	}

	private UserAccount getUserAccount(final User user) {
		return ConnectionFactory.getInstance().withUserReadOnly(
				new UserTransaction<UserAccount>() {

					public UserAccount perform(final Connection conn,
							final Server server, final User serverUser)
							throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						final String userName = user.getName();
						return new UserAccount(user.getId(), userName, server
								.getName(), man.isUserInGroup(userName,
								SierraGroup.ADMIN.getName()), user.isActive());
					}
				});
	}

}
