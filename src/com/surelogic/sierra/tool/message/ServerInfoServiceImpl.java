package com.surelogic.sierra.tool.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class ServerInfoServiceImpl extends SRPCServlet implements
		ServerInfoService {
	private static final long serialVersionUID = 557394723869102797L;

	private final ServerInfoReply reply = new ServerInfoReply();

	public ServerInfoReply getServerInfo(ServerInfoRequest request) {
		synchronized (reply) {
			return reply;
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		synchronized (reply) {
			final List<Services> services = reply.getServices();
			if ("on".equals(config.getServletContext().getInitParameter(
					"teamserver"))) {
				services.add(Services.TEAMSERVER);
			}
			if ("on".equals(config.getServletContext().getInitParameter(
					"buglink"))) {
				services.add(Services.BUGLINK);
			}
			reply.setUid(ConnectionFactory
					.withReadOnly(new ServerTransaction<String>() {

						public String perform(Connection conn, Server server)
								throws SQLException {
							return server.getUid();
						}
					}));
		}
	}

}
