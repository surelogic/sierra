package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.ManageServerService;
import com.surelogic.sierra.gwt.client.ServerInfo;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.User;

public class ManageServerServiceImpl extends SierraServiceServlet implements
		ManageServerService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4174730158310056800L;

	public ServerInfo deploySchema() {
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<ServerInfo>() {

					public ServerInfo perform(Connection conn, Server server, User user)
							throws SQLException {
						try {
							server.updateSchema();
						} catch (FutureDatabaseException e) {
							//Do Nothing
						}
						return readServerInfo(server);
					}
				});

	}

	public ServerInfo getServerInfo() {
		return ConnectionFactory.withUserReadOnly(new UserTransaction<ServerInfo>() {

			public ServerInfo perform(Connection conn, Server server, User user)
					throws SQLException {
				return readServerInfo(server);
			}
		});
	}

	public ServerInfo setEmail(final String address) {
		return ConnectionFactory
				.withUserTransaction(new UserTransaction<ServerInfo>() {

					public ServerInfo perform(Connection conn, Server server, User user)
							throws SQLException {
						server.setEmail(address);
						return readServerInfo(server);
					}
				});
	}

	private ServerInfo readServerInfo(Server server) {
		ServerInfo info = new ServerInfo();
		info.setAvailableVersion(server.getAvailableSchemaVersion());
		try {
			info.setCurrentVersion(server.getSchemaVersion());
		} catch (SQLException e) {
			info.setCurrentVersion("Error");
		}
		try {
			info.setEmail(server.getEmail());
		} catch (SQLException e) {
			info.setEmail("Error");
		}
		return info;
	}

}
