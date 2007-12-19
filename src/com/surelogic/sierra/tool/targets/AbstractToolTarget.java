package com.surelogic.sierra.tool.targets;

import java.net.URL;

public abstract class AbstractToolTarget implements IToolTarget {
  private final boolean isSource;
  protected final URL location;

  protected AbstractToolTarget(boolean isSrc, URL loc) {
    location = loc;
    isSource = isSrc;
  }
  
  public final boolean isSource() {
    return isSource;
  }

  public final URL getLocation() {
    return location;
  }
}
