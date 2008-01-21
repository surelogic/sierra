package com.surelogic.sierra.tool.targets;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public abstract class DirectoryTarget extends AbstractToolTarget {
  /**
   * For JAXB
   */
  protected DirectoryTarget() {    
  }
  
  protected DirectoryTarget(Type type, URI loc) {
    super(type, loc);
  }
  
  public final Kind getKind() {
    return Kind.DIRECTORY;
  }

  public Iterable<URI> getFiles() {
    List<URI> files = new ArrayList<URI>();
    findFiles(files, new File(getLocation()));
    return files;
  }

  private void findFiles(List<URI> files, File here) {   
    if (!here.exists()) {
      return;
    }
    URI uri = here.toURI();
    String uriPath = uri.getPath();
    String locationPath = location.getPath();
    if (!uriPath.startsWith(locationPath)) {
      throw new IllegalArgumentException(uri+" isn't under "+location);
    }
    String relativePath;
    if (uriPath.equals(locationPath)) {
      relativePath = "";
    } else {
      relativePath = uriPath.substring(locationPath.length()+1);
    }
    if (exclude(relativePath)) {
      return;
    }
    if (here.isDirectory()) {
      for(File f : here.listFiles()) {
        findFiles(files, f);
      }
    }
    else if (here.isFile()) {
      if (isSource()) {
        if (here.getName().endsWith(".java")) {
          files.add(here.toURI());
        }
      } else if (here.getName().endsWith(".class")) {
        files.add(here.toURI());
      }
    }
  }  
}
