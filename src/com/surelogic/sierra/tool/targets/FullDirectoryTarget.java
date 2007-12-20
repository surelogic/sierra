package com.surelogic.sierra.tool.targets;

import java.net.URI;

public class FullDirectoryTarget extends DirectoryTarget {
  public FullDirectoryTarget(boolean isSrc, URI loc) {
    super(isSrc, loc);
  }

  public boolean exclude(String relativePath) {
    return false;
  }
}
