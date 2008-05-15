package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

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
		return reply;
	}

	public static void registerService(Services service) {
		synchronized (services) {
			services.add(service);
		}
	}

}
