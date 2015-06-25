package com.surelogic.sierra.message.srpc;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
class InvalidVersion {

  protected String serverVersion;

  protected String clientVersion;

  public InvalidVersion() {
  }

  public InvalidVersion(final String serverVersion, final String clientVersion) {
    this.serverVersion = serverVersion;
    this.clientVersion = clientVersion;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public void setServerVersion(final String serverVersion) {
    this.serverVersion = serverVersion;
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public void setClientVersion(final String clientVersion) {
    this.clientVersion = clientVersion;
  }

}
