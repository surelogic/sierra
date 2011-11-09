package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

@Service(version = "2.2.1")
public interface ServerInfoService {

	ServerInfoReply getServerInfo(ServerInfoRequest request);

}
