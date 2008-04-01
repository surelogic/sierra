package com.surelogic.sierra.tool.registration;

import com.surelogic.sierra.message.srpc.SRPCClient;
import com.surelogic.sierra.tool.message.SierraServerLocation;

/**
 * Utility class for generating proxies that call the SureLogic registration
 * web service.
 * 
 * The objects returned by this class will implement the {@link Registration}
 * interface. In addition, the following unchecked exceptions may be thrown:
 * {@link SierraServiceClientException} .
 * 
 * @author Edwin
 * 
 */
public class RegistrationClient {

	public static Registration create() {
		return SRPCClient.createClient(SierraServerLocation.DEFAULT,
				Registration.class, true);
	}

	public static Registration create(SierraServerLocation location) {
		return SRPCClient.createClient(location, Registration.class, true);
	}
}
