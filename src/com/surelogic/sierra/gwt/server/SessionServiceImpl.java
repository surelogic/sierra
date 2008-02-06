package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.SessionService;
import com.surelogic.sierra.gwt.client.UserAccount;
import com.surelogic.sierra.gwt.client.data.LoginResult;
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
								throws Exception {
							return ServerUserManager.getInstance(conn).login(
									userName, password);
						}
					});
			if (u != null) {
				getThreadLocalRequest().getSession().setAttribute(
						SecurityHelper.USER, u);
				UserContext.set(u);
				return new LoginResult(getUserAccount(u));
			} else {
				return new LoginResult("Invalid username or password");
			}
		} else {
			return new LoginResult("Missing username or password");
		}
	}

	private UserAccount getUserAccount(final User user) {
		return ConnectionFactory
				.withUserReadOnly(new UserTransaction<UserAccount>() {

					public UserAccount perform(Connection conn, Server server,
							User serverUser) throws SQLException {
						final ServerUserManager man = ServerUserManager
								.getInstance(conn);
						final String userName = user.getName();
						return new UserAccount(userName, man.isUserInGroup(
								userName, SierraGroup.ADMIN.getName()));
					}
				});
	}
}
