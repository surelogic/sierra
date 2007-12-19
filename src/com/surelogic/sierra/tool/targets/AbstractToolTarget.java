package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class AbstractToolTarget implements IToolTarget {
  private final boolean isSource;
  protected final URI location;

  protected AbstractToolTarget(boolean isSrc, URI loc) {
    location = loc;
    isSource = isSrc;
  }
  
  public final boolean isSource() {
    return isSource;
  }

  public final URI getLocation() {
    return location;
  }
}
