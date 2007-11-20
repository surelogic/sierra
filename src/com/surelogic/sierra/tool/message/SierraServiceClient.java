package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.tool.message.axis.Axis2Client;

public class SierraServiceClient {

	public static SierraService create() {
		return new Axis2Client();
	}

	public static SierraService create(SierraServerLocation location) {
		return new Axis2Client(location);
	}

}
