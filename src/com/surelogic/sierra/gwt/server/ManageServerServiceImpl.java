package com.surelogic.sierra.gwt.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.service.ManageServerService;
import com.surelogic.sierra.jdbc.server.Notification;
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
		return performAdmin(false, new UserTransaction<ServerInfo>() {

			public ServerInfo perform(final Connection conn,
					final Server server, final User user) throws SQLException {
				try {
					server.updateSchema();
				} catch (final FutureDatabaseException e) {
					// Do Nothing
				}
				return readServerInfo(server);
			}
		});
	}

	public ServerInfo getServerInfo() {
		return performAdmin(true, new UserTransaction<ServerInfo>() {

			public ServerInfo perform(final Connection conn,
					final Server server, final User user) throws SQLException {
				return readServerInfo(server);
			}
		});
	}

	public ServerInfo setSiteName(final String name) {
		return performAdmin(false, new UserTransaction<ServerInfo>() {
			public ServerInfo perform(final Connection conn,
					final Server server, final User user) throws SQLException {
				server.setName(name, server.nextRevision());
				return readServerInfo(server);
			}
		});
	}

	public ServerInfo setEmail(final EmailInfo info) {
		return performAdmin(false, new UserTransaction<ServerInfo>() {

			public ServerInfo perform(final Connection conn,
					final Server server, final User user) throws SQLException {
				final String portStr = info.getPort();
				final Integer port = portStr == null ? null : Integer
						.valueOf(portStr);
				server.setNotification(new Notification(info.getHost(), port,
						info.getUser(), info.getPass(), info.getAdminEmail(),
						info.getServerEmail()));
				return readServerInfo(server);
			}
		});
	}

	public void testAdminEmail() {
		performAdmin(true, new UserTransaction<Void>() {

			public Void perform(final Connection conn, final Server server,
					final User user) throws Exception {
				server
						.notifyAdmin(
								"This is a test.",
								"If you received this email, server exception notification is configured properly.");
				return null;
			}
		});
	}

	private ServerInfo readServerInfo(final Server server) throws SQLException {
		final ServerInfo info = new ServerInfo();
		info.setSiteName(server.getName());
		info.setProductVersion(Server.getSoftwareVersion());
		info.setAvailableVersion(server.getAvailableSchemaVersion());
		try {
			info.setCurrentVersion(server.getSchemaVersion());
		} catch (final SQLException e) {
			info.setCurrentVersion("Error");
		}
		try {
			final Notification n = server.getNotification();
			if (n != null) {
				final Integer port = n.getPort();
				info.setEmail(new EmailInfo(n.getHost(), port == null ? null
						: port.toString(), n.getUser(), n.getPassword(), n
						.getFromEmail(), n.getToEmail()));
			} else {
				info.setEmail(new EmailInfo());
			}
		} catch (final SQLException e) {
			final String error = "Error";
			info.setEmail(new EmailInfo(error, error, error, error, error,
					error));
		}
		return info;
	}
}
