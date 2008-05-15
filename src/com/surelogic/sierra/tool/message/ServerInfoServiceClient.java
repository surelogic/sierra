package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.SRPCClient;

public class ServerInfoServiceClient {
	public static ServerInfoService create() {
		return SRPCClient.createClient(SierraServerLocation.DEFAULT,
				ServerInfoService.class, true);
	}

	public static ServerInfoService create(SierraServerLocation location) {
		return SRPCClient.createClient(location, ServerInfoService.class, true);
	}
}
