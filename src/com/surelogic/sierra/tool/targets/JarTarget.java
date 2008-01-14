package com.surelogic.sierra.tool.targets;

import java.net.*;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class JarTarget extends AbstractToolTarget {
  /**
   * For JAXB
   */
  public JarTarget() {}
  
  public JarTarget(Type type, URI loc) {
    super(type, loc);
  }
  
  public JarTarget(URI loc) {
    super(Type.AUX, loc);
  }
  
  public final Kind getKind() {
    return Kind.JAR;
  }

  public boolean exclude(String relativePath) {
    return false;
  }

  public Iterable<URI> getFiles() {
    throw new UnsupportedOperationException();
  }  
}
