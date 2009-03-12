package com.surelogic.sierra.tool.findbugs;

import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.IFindBugsEngine;

public class FindBugs1_3_1Tool extends AbstractFindBugsTool {
  public FindBugs1_3_1Tool(String fbDir, boolean debug) {
    super("1.3.1", fbDir, debug);
  }
  
  @Override
  protected IFindBugsEngine createEngine() {
    return new FindBugs2();
  }
}
