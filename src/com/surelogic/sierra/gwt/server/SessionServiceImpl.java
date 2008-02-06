package com.surelogic.sierra.gwt.server;

import java.sql.Connection;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.SessionService;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.SecurityHelper;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.jdbc.server.UserContext;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.User;

public class SessionServiceImpl extends SierraServiceServlet implements
		SessionService {
	private static final long serialVersionUID = -6665683550116264375L;

	public boolean isValidSession() {
		return UserContext.peek() != null;
	}

	public String login(final String userName, final String password) {
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
				getThreadLocalRequest().getSession().setAttribute(SecurityHelper.USER,
						u);
				UserContext.set(u);
				return null;
			} else {
				return "Invalid username or password";
			}
		} else {
			return "Missing username or password";
		}
	}

}
