package com.surelogic.sierra.tool.message;

import java.util.Arrays;

import com.surelogic.sierra.message.srpc.SRPCServlet;

public class ServerInfoServiceImpl extends SRPCServlet implements ServerInfoService {
	private static final long serialVersionUID = 557394723869102797L;

	public ServerInfoReply getServerInfo(ServerInfoRequest request) {
		final ServerInfoReply reply = new ServerInfoReply();
		reply.getServices().addAll(
				Arrays.asList(new Services[] { Services.BUGLINK,
						Services.TEAMSERVER }));
		return reply;
	}

}
