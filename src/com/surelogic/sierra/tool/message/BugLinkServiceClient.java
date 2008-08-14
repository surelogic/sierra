package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.SRPCClient;

/**
 * Utility class for generating proxies that call the Sierra client web service.
 * The objects returned by this class will implement the {@link BuglinkService}
 * interface. In addition, two unchecked exceptions may be thrown:
 * {@link SierraServiceClientException} and {@link InvalidLoginException}.
 * 
 * @author nathan
 * 
 */
public class BugLinkServiceClient {

	public static BugLinkService create() {
		return SRPCClient.createClient(SierraServerLocation.DEFAULT,
				BugLinkService.class, true);
	}

	public static BugLinkService create(final SierraServerLocation location) {
		return SRPCClient.createClient(location, BugLinkService.class, true);
	}
}
