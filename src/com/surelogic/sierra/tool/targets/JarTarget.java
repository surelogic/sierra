package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class JarTarget extends AbstractToolTarget {
  protected JarTarget(boolean isSrc, URI loc) {
    super(isSrc, loc);
  }
  
  protected JarTarget(URI loc) {
    super(false, loc);
  }
  
  public final Kind getKind() {
    return Kind.JAR;
  }  
}
