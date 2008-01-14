package com.surelogic.sierra.tool.targets;

import java.net.URI;

public class FullDirectoryTarget extends DirectoryTarget {
  public FullDirectoryTarget(Type type, URI loc) {
    super(type, loc);
  }

  public boolean exclude(String relativePath) {
    return false;
  }
}
