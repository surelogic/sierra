package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.SRPCClient;

/**
 * Utility class for generating proxies that call the Sierra client web service.
 * The objects returned by this class will implement the {@link SupportService}
 * interface.
 * 
 * @author Edwin
 */
public class SupportServiceClient {

	public static SupportService create(final SierraServerLocation location) {
		return SRPCClient.createClient(location, SupportService.class, true);
	}
}
