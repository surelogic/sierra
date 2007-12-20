package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class AbstractToolTarget implements IToolTarget {
  private final Type type;
  protected final URI location;

  protected AbstractToolTarget(Type t, URI loc) {
    location = loc;
    type = t;
  }
  
  public final boolean isSource() {
    return type == Type.SOURCE;
  }
  
  public final Type getType() {
    return type;
  }

  public final URI getLocation() {
    return location;
  }
}
