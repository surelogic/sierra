package com.surelogic.sierra.tool.targets;

import java.net.URL;

public abstract class JarTarget extends AbstractToolTarget {
  protected JarTarget(boolean isSrc, URL loc) {
    super(isSrc, loc);
  }
  
  protected JarTarget(URL loc) {
    super(false, loc);
  }
  
  public final Kind getKind() {
    return Kind.JAR;
  }  
}
