package com.surelogic.sierra.tool.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.surelogic.common.jdbc.DBQuery;
import com.surelogic.common.jdbc.Query;
import com.surelogic.common.jdbc.Row;
import com.surelogic.common.jdbc.RowHandler;
import com.surelogic.sierra.jdbc.server.ConnectionFactory;
import com.surelogic.sierra.jdbc.server.Server;
import com.surelogic.sierra.jdbc.server.ServerTransaction;
import com.surelogic.sierra.message.srpc.SRPCServlet;

public class ServerInfoServiceImpl extends SRPCServlet implements
		ServerInfoService {
	private static final long serialVersionUID = 557394723869102797L;

	private transient final ServerInfoReply reply = new ServerInfoReply();

	public ServerInfoReply getServerInfo(final ServerInfoRequest request) {
		/*
		 * Copy info from the reply we stored, and add the servers to it.
		 */
		final ServerInfoReply reply = new ServerInfoReply();
		reply.setServices(this.reply.getServices());
		reply.setServers(ConnectionFactory.getInstance().withReadOnly(
				new DBQuery<List<ServerIdentity>>() {
					public List<ServerIdentity> perform(final Query q) {
						return q.prepared("ServerLocations.listIdentities",
								new RowHandler<ServerIdentity>() {
									public ServerIdentity handle(final Row r) {
										return new ServerIdentity(r
												.nextString(), r.nextString(),
												r.nextLong());
									}
								}).call();
					}
				}));
		reply.setUid(this.reply.getUid());
		return reply;
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		synchronized (reply) {
			final List<Services> services = reply.getServices();
			if ("on".equals(config.getServletContext().getInitParameter(
					"teamserver"))) {
				services.add(Services.TEAMSERVER);
			}
			// XXX for now buglink is always on
			services.add(Services.BUGLINK);
			reply.setUid(ConnectionFactory.getInstance().withReadOnly(
					new ServerTransaction<String>() {

						public String perform(final Connection conn,
								final Server server) throws SQLException {
							return server.getUid();
						}
					}));

		}
	}

}
