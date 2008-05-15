package com.surelogic.sierra.tool.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.UserTransaction;
import com.surelogic.sierra.jdbc.user.User;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class ServerInfoServiceImpl extends SRPCServlet implements
		ServerInfoService {
	private static final long serialVersionUID = 557394723869102797L;

	private static final List<Services> services = new ArrayList<Services>();

	public ServerInfoReply getServerInfo(ServerInfoRequest request) {
		final ServerInfoReply reply = new ServerInfoReply();
		synchronized (services) {
			reply.getServices().addAll(services);
		}
		reply.setUid(ConnectionFactory
				.withUserReadOnly(new UserTransaction<String>() {

					public String perform(Connection conn, Server server,
							User user) throws SQLException {
						return server.getUid();
					}
				}));
		return reply;
	}

	public static void registerService(Services service) {
		synchronized (services) {
			services.add(service);
		}
	}
}
