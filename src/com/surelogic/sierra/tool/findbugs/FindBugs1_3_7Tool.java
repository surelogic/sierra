package com.surelogic.sierra.tool.findbugs;

import java.util.Collections;
import java.util.Set;

import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;

public class FindBugs1_3_7Tool extends AbstractFindBugsTool {
  public FindBugs1_3_7Tool(String fbDir, boolean debug) {
    super("1.3.7", fbDir, debug);
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  @Override
  protected IFindBugsEngine createEngine() {
    return new FindBugs2();
  }
}
