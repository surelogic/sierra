package com.surelogic.sierra.tool.pmd;

import java.util.*;

public class PMD4_0Tool extends AbstractPMDTool {
  public PMD4_0Tool(boolean debug) {
    super("4.0", debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
}
