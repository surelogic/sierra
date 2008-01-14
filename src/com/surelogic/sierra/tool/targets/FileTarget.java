package com.surelogic.sierra.tool.targets;

import java.net.*;
import java.util.*;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

/**
 * A target for a single file
 * 
 * @author Edwin.Chan
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public final class FileTarget extends AbstractToolTarget {
  /**
   * For JAXB
   */
  public FileTarget() {}
  
  public FileTarget(Type type, URI loc) {
    super(type, loc);
  }
  
  public FileTarget(URI loc) {
    super(Type.SOURCE, loc);
  }
  
  public boolean exclude(String relativePath) {
    return false;
  }

  public final Kind getKind() {
    return Kind.FILE;
  }

  public Iterable<URI> getFiles() {
    List<URI> l = new ArrayList<URI>(1);
    l.add(getLocation());
    return l;
    /*
    return new Iterator<URI>() {
      private boolean done;
      public boolean hasNext() {
        return !done;
      }

      public URI next() {
        if (done) {
          throw new NoSuchElementException();
        }
        done = true;
        return getLocation();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }};
      */
  }
}
