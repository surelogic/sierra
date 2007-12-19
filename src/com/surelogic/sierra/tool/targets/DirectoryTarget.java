package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class DirectoryTarget extends AbstractToolTarget {
  protected DirectoryTarget(boolean isSrc, URI loc) {
    super(isSrc, loc);
  }
  
  public final Kind getKind() {
    return Kind.DIRECTORY;
  }  
}
