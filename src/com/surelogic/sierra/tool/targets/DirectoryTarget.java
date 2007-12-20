package com.surelogic.sierra.tool.targets;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DirectoryTarget extends AbstractToolTarget {
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
