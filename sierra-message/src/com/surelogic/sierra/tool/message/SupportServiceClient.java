package com.surelogic.sierra.tool.message;

import com.surelogic.sierra.message.srpc.MultiPartSRPCClient;

/**
 * Utility class for generating proxies that call the Sierra client web service.
 * The objects returned by this class will implement the {@link SupportService}
 * interface.
 */
public class SupportServiceClient {

  public static SupportService create(final ServerLocation location) {
    return MultiPartSRPCClient.createClient(location, SupportService.class, true);
  }
}
