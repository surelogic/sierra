package com.surelogic.sierra.tool.findbugs;

import java.util.Collections;
import java.util.Set;

import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;

public class FindBugs1_3_0Tool extends AbstractFindBugsTool {
  public FindBugs1_3_0Tool() {
    super("1.3.0");
  }

  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  @Override
  protected IFindBugsEngine createEngine() {
    return new FindBugs2();
  }
}
