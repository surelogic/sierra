package com.surelogic.sierra.gwt.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.common.jdbc.FutureDatabaseException;
import com.surelogic.common.jdbc.TransactionException;
import com.surelogic.sierra.gwt.SierraServiceServlet;
import com.surelogic.sierra.gwt.client.data.EmailInfo;
import com.surelogic.sierra.gwt.client.data.ServerInfo;
import com.surelogic.sierra.gwt.client.data.Status;
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
		if (info.isValid()) {
			return performAdmin(false, new UserTransaction<ServerInfo>() {

				public ServerInfo perform(final Connection conn,
						final Server server, final User user)
						throws SQLException {
					final String portStr = info.getPort();
					final Integer port = portStr == null ? null : Integer
							.valueOf(portStr);
					server.setNotification(new Notification(info.getHost(),
							port, info.getUser(), info.getPass(), info
									.getAdminEmail(), info.getServerEmail()));
					return readServerInfo(server);
				}
			});
		} else {
			return null;
		}
	}

	public Status testAdminEmail() {
		try {
			performAdmin(true, new UserTransaction<Void>() {

				public Void perform(final Connection conn, final Server server,
						final User user) throws Exception {
					final String name = server.getName();
					server
							.notifyAdmin(
									String
											.format(
													"Sierra Team Server '%s' Email Test",
													name),
									String
											.format(
													"If you received this email, server exception notification is configured properly for '%s'",
													name));
					return null;
				}
			});
			return new Status(
					true,
					"Test message sent.  If your notification settings are correct, you should receive a message shortly.");
		} catch (final TransactionException e) {
			return new Status(
					false,
					String
							.format(
									"An error occurred while trying to send a test message.  See the server log for more details: %s",
									e.getMessage()));
		}
	}

	private ServerInfo readServerInfo(final Server server) throws SQLException {
		final ServerInfo info = new ServerInfo();
		InetAddress a;
		try {
			a = InetAddress.getLocalHost();
			if (a != null) {
				info.setHostName(a.getHostName());
			}
		} catch (final UnknownHostException e1) {
			// Do nothing
		}
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
