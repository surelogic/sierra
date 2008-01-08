package com.surelogic.sierra.tool.targets;

import java.net.*;

public abstract class AbstractToolTarget implements IToolTarget {
  private final Type type;
  protected final URI location;
  protected final IToolTarget auxSrcLocation;

  protected AbstractToolTarget(Type t, URI loc) {
    this(t, loc, null);
  }
  
  protected AbstractToolTarget(Type t, URI loc, IToolTarget auxSrcLoc) {
    location = loc;
    type = t;
    if (auxSrcLoc == null || type == Type.AUX) {          
      auxSrcLocation = auxSrcLoc;
    } else {
      throw new IllegalArgumentException(type+" can't have an aux source location: "+auxSrcLoc);
    }
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
  
  public final IToolTarget getAuxSources() {
    return auxSrcLocation;
  }
}
