package com.surelogic.sierra.tool.registration;

import com.surelogic.sierra.message.srpc.MultiPartSRPCClient;
import com.surelogic.sierra.tool.message.ServerLocation;
import com.surelogic.sierra.tool.message.SierraServiceClientException;

/**
 * Utility class for generating proxies that call the SureLogic registration web
 * service.
 * 
 * The objects returned by this class will implement the {@link Registration}
 * interface. In addition, the following unchecked exceptions may be thrown:
 * {@link SierraServiceClientException} .
 */
public class RegistrationClient {

  public static Registration create(final ServerLocation location) {
    return MultiPartSRPCClient.createClient(location, Registration.class, true);
  }
}
