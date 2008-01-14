package com.surelogic.sierra.tool.targets;

import java.net.*;

public class JarTarget extends AbstractToolTarget {
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
