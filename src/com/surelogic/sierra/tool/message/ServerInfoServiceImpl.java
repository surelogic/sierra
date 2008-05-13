package com.surelogic.sierra.tool.message;

import java.util.Arrays;

public class ServerInfoServiceImpl implements ServerInfoService {

	public ServerInfoReply getServerInfo(ServerInfoRequest request) {
		final ServerInfoReply reply = new ServerInfoReply();
		reply.getServices().addAll(
				Arrays.asList(new Services[] { Services.BUGLINK,
						Services.TEAMSERVER }));
		return reply;
	}

}
