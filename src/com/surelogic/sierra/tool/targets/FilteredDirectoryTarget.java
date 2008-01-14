package com.surelogic.sierra.tool.targets;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class FilteredDirectoryTarget extends DirectoryTarget {
  /**
   * For JAXB
   */
  public FilteredDirectoryTarget() {}
  
  private String[] exclusions; 
  
  public FilteredDirectoryTarget(Type type, URI loc, String... exclusions) {
    super(type, loc);
    this.exclusions = exclusions;
  }

  public boolean exclude(String relativePath) {
    for(String ex : exclusions) {
      if (relativePath.startsWith(ex)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * For JAXB 
   */
  public void setExclusions(String[] ex) {
    exclusions = ex;
  }
    
  public String[] getExclusions() {
    return exclusions;
  }
}
