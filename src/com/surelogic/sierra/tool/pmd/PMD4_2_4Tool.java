package com.surelogic.sierra.tool.pmd;

import java.util.Collections;
import java.util.Set;

public class PMD4_2_4Tool extends AbstractPMDTool {
  public PMD4_2_4Tool(boolean debug) {
    super("4.2.4", debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
}
