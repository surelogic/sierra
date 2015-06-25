package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EnsureExtensionResponse {
  protected List<ExtensionName> unknownExtensions;

  public List<ExtensionName> getUnknownExtensions() {
    if (unknownExtensions == null) {
      unknownExtensions = new ArrayList<>();
    }
    return unknownExtensions;
  }

}
