package com.surelogic.sierra.tool.targets;

import java.net.URL;

public abstract class DirectoryTarget extends AbstractToolTarget {
  protected DirectoryTarget(boolean isSrc, URL loc) {
    super(isSrc, loc);
  }
  
  public final Kind getKind() {
    return Kind.DIRECTORY;
  }  
}
