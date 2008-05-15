package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.Service;

public interface ServerInfoService extends Service {

	ServerInfoReply getServerInfo(ServerInfoRequest request);

}
