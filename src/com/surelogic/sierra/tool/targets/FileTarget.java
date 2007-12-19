package com.surelogic.sierra.tool.targets;

import java.net.URL;

/**
 * A target for a single file
 * 
 * @author Edwin.Chan
 */
public final class FileTarget extends AbstractToolTarget {
  public FileTarget(boolean isSrc, URL loc) {
    super(isSrc, loc);
  }
  
  public FileTarget(URL loc) {
    super(true, loc);
  }
  
  public boolean exclude(String relativePath) {
    return false;
  }

  public final Kind getKind() {
    return Kind.FILE;
  }
}
