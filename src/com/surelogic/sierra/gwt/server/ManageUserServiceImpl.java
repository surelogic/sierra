package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.ManageUserService;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerConnection;
import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.User;

public class ManageUserServiceImpl extends SierraServiceServlet implements
		ManageUserService {

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
				if (man.createUser(user, password)) {
					return user + " created.";
				} else {
					return "Could not create user with name " + user + ".";
				}
			}
		});
	}

	public List<String> findUser(final String userQueryString) {
		return ServerConnection
				.withTransaction(new WebTransaction<List<String>>() {

					public List<String> perform(Connection conn, Server server)
							throws SQLException {
						final List<User> users = ServerUserManager.getInstance(
								conn).findUser(userQueryString);
						final List<String> userNames = new ArrayList<String>(
								users.size());
						for (User u : users) {
							userNames.add(u.getName());
						}
						return userNames;
					}
				});
	}

	public List<String> getUsers() {
		return ServerConnection
				.withTransaction(new WebTransaction<List<String>>() {

					public List<String> perform(Connection conn, Server server)
							throws SQLException {
						final List<User> users = ServerUserManager.getInstance(
								conn).listUsers();
						final List<String> userNames = new ArrayList<String>(
								users.size());
						for (User u : users) {
							userNames.add(u.getName());
						}
						return userNames;
					}
				});
	}

}
