package com.surelogic.sierra.tool.targets;

import java.net.URI;

public class FilteredDirectoryTarget extends DirectoryTarget {
  private final String[] exclusions; 
  public FilteredDirectoryTarget(Type type, URI loc, String[] exclusions) {
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
}
