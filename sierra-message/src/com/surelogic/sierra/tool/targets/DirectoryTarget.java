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

  @Override
  public final Kind getKind() {
    return Kind.DIRECTORY;
  }

  @Override
  public Iterable<URI> getFiles() {
    List<URI> files = new ArrayList<>();
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
      throw new IllegalArgumentException(uri + " isn't under " + location);
    }
    String relativePath;
    if (uriPath.equals(locationPath)) {
      relativePath = "";
    } else {
      relativePath = uriPath.substring(locationPath.endsWith("/") ? locationPath.length() : locationPath.length() + 1);
    }
    // System.out.println("Looking at "+relativePath+" in "+locationPath);
    if (exclude(relativePath)) {
      return;
    }
    if (here.isDirectory()) {
      for (File f : here.listFiles()) {
        findFiles(files, f);
      }
    } else if (here.isFile()) {
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
