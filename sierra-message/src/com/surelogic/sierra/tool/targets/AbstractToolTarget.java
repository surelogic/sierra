package com.surelogic.sierra.tool.targets;

import java.net.*;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public abstract class AbstractToolTarget extends ToolTarget {
  private Type type;
  protected URI location;
  protected ToolTarget auxSources;

  /**
   * For JAXB
   */
  protected AbstractToolTarget() {    
  }
  
  protected AbstractToolTarget(Type t, URI loc) {
    this(t, loc, null);
  }
  
  protected AbstractToolTarget(Type t, URI loc, ToolTarget auxSrcLoc) {
    location = loc;
    type = t;
    if (auxSrcLoc == null || type == Type.AUX) {          
      auxSources = auxSrcLoc;
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
    return auxSources;
  }
  
  @Override
  public String toString() {
    return location.toASCIIString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof IToolTarget) {
      IToolTarget t = (IToolTarget) o;
      return location.equals(t.getLocation());
    }
    return false; 
  }
  
  @Override
  public int hashCode() {
    return location.hashCode();
  }
  
  /**
   * For JAXB
   */
  public final void setType(Type t) {
    type = t;
  }

  public final void setLocation(URI l) {
    location = l;
  }
  
  public final void getAuxSources(ToolTarget t) {
    auxSources = t;
  }
}
