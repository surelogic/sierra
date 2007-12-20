package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class JarTarget extends AbstractToolTarget {
  protected JarTarget(Type type, URI loc) {
    super(type, loc);
  }
  
  protected JarTarget(URI loc) {
    super(Type.AUX, loc);
  }
  
  public final Kind getKind() {
    return Kind.JAR;
  }  
}
