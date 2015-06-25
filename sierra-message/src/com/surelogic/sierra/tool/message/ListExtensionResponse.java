package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ListExtensionResponse {
  protected List<Extension> extension;

  public List<Extension> getExtensions() {
    if (extension == null) {
      extension = new ArrayList<>();
    }
    return extension;
  }

  public void setExtensions(final List<Extension> extension) {
    this.extension = extension;
  }

}
