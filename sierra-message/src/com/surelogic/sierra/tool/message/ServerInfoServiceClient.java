package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.MultiPartSRPCClient;

/**
 * Utility class for generating proxies that call the Sierra client web service.
 * The objects returned by this class will implement the
 * {@link ServerInfoService} interface. In addition, two unchecked exceptions
 * may be thrown: {@link SierraServiceClientException} and
 * {@link InvalidLoginException}.
 * 
 * @author nathan
 * 
 */
public class ServerInfoServiceClient {

  public static ServerInfoService create(final ServerLocation location) {
    return MultiPartSRPCClient.createClient(location, ServerInfoService.class, true);
  }
}
