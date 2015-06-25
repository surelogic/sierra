package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadExtensionRequest {
  protected Extension e;

  public Extension getE() {
    return e;
  }

  public void setE(final Extension e) {
    this.e = e;
  }

}
