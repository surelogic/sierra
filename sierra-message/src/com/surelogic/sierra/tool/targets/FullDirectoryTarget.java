package com.surelogic.sierra.tool.targets;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class FullDirectoryTarget extends DirectoryTarget {
  /**
   * For JAXB
   */
  public FullDirectoryTarget() {}
  
  public FullDirectoryTarget(Type type, URI loc) {
    super(type, loc);
  }

  public boolean exclude(String relativePath) {
    return false;
  }
}
