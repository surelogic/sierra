package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.LoginResult;
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

	public UserAccount getUserAccount() {
		final User u = UserContext.peek();
		if (u == null) {
			return null;
		}

		return getUserAccount(u);
	}

	public LoginResult login(final String userName, final String password) {
		if (userName != null && password != null) {
			final User u = ConnectionFactory
					.withReadOnly(new ServerTransaction<User>() {

						public User perform(Connection conn, Server server)
								throws SQLException {
							return ServerUserManager.getInstance(conn).login(
									userName, password);
						}
					});
			if (u != null) {
				getThreadLocalRequest().getSession().setAttribute(
						SecurityHelper.USER, u);
				UserContext.set(u);
				log.info("User " + userName + "logged in successfully");
				return new LoginResult(getUserAccount(u));
			} else {
				log.info("Failed logging attempt for user " + userName);
				return new LoginResult("Invalid username or password");
			}
		} else {
			return new LoginResult("Missing username or password");
		}
	}

	public void logout() {
		try {
			getThreadLocalRequest().getSession().invalidate();
		} catch (IllegalStateException ise) {
			// ignore exception if already logged out
		}
		UserContext.set(null);
	}

	private UserAccount getUserAccount(final User user) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<UserAccount>() {

					public UserAccount perform(Connection conn, Server server,
							User serverUser) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						final String userName = user.getName();
						return new UserAccount(user.getId(), userName, man
								.isUserInGroup(userName, SierraGroup.ADMIN
										.getName()), user.isActive());
					}
				});
	}

}
