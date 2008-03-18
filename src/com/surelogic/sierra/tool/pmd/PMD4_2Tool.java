package com.surelogic.sierra.tool.pmd;

import java.util.Collections;
import java.util.Set;

public class PMD4_2Tool extends AbstractPMDTool {
  public PMD4_2Tool(boolean debug) {
    super("4.2", debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
}
