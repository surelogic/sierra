package com.surelogic.sierra.tool;

import com.surelogic.sierra.tool.findbugs.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.pmd.*;
import com.surelogic.sierra.tool.reckoner.*;

public class ToolUtil {
  public static ITool create(Config config) {
    // FIX look at config
    return create(config, false);
  }
  
  public static ITool create(Config config, boolean runRemotely) {
    if (runRemotely) {
      return new LocalTool();
    }
    final MultiTool t = new MultiTool();
    if (!config.getExcludedToolsList().contains("findbugs")) {
      t.addTool(new FindBugs1_3_0Tool());
    }
    if (!config.getExcludedToolsList().contains("pmd")) {
      t.addTool(new PMD4_0Tool());
    }
    if (!config.getExcludedToolsList().contains("reckoner")) {
      t.addTool(new Reckoner1_0Tool());
    }
    return t;
  }
}
