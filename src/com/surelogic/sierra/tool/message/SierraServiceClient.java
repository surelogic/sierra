package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.tool.message.jaxws.JAXWSClient;

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
	private SierraServiceClient() {
	}

	public static SierraService create() {
		return new JAXWSClient();
	}

	public static SierraService create(SierraServerLocation location) {
		return new JAXWSClient(location);
	}
}
