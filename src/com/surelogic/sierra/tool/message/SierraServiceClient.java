package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.SRPCClient;

/**
 * Utility class for generating proxies that call the Sierra client web service.
 * The objects returned by this class will implement the {@link SierraService}
 * interface. In addition, two unchecked exceptions may be thrown:
 * {@link SierraServiceClientException} and {@link InvalidLoginException}.
 * 
 * @author nathan
 * 
 */
public class SierraServiceClient {

	public static SierraService create() {
		return SRPCClient.createClient(SierraServerLocation.DEFAULT,
				SierraService.class, true);
	}

	public static SierraService create(SierraServerLocation location) {
		return SRPCClient.createClient(location, SierraService.class, true);
	}
}
