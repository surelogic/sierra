package com.surelogic.sierra.tool.pmd;

import java.util.Collections;
import java.util.Set;

public class CPD4_1Tool extends AbstractCPDTool {
  public CPD4_1Tool(boolean debug) {
    super("4.1", debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
}
