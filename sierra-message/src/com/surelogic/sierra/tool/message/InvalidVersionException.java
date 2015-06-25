package com.surelogic.sierra.tool.message;

public class InvalidVersionException extends SierraServiceClientException {

  private static final long serialVersionUID = 2535569879937901044L;

  private final String serviceVersion;
  private final String clientVersion;

  /**
   * 
   * @param service
   *          the version number of the service
   * @param client
   *          the version number the client passed in
   */
  public InvalidVersionException(final String service, final String client) {
    super(String.format("The server (version %s) and the client (version %s) use different versions of this protocol.", service,
        client));
    this.serviceVersion = service;
    this.clientVersion = client;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public String getClientVersion() {
    return clientVersion;
  }

}
