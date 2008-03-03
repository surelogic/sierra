package com.surelogic.sierra.tool.targets;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public abstract class ToolTarget implements IToolTarget {
  protected ToolTarget() {    
  }
  @Override
  public abstract boolean equals(Object o);
  @Override
  public abstract int hashCode();
}
