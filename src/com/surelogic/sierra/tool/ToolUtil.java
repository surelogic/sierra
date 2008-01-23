package com.surelogic.sierra.tool;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.findbugs.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.pmd.*;
import com.surelogic.sierra.tool.reckoner.*;

public class ToolUtil {
  /** The logger */
  protected static final Logger LOG = SLLogger.getLogger("sierra");
  
  public static ITool create(Config config) {
    // FIX look at config
    return create(config, true);
  }
  
  public static ITool create(Config config, boolean runRemotely) {
    if (runRemotely) {
      return new LocalTool();
    }
    final MultiTool t = new MultiTool();
    if (!config.getExcludedToolsList().contains("findbugs")) {
      t.addTool(new FindBugs1_3_0Tool(config.getPluginDir(SierraToolConstants.FB_PLUGIN_ID)));
    }
    if (!config.getExcludedToolsList().contains("pmd")) {
      t.addTool(new PMD4_0Tool());
    }
    if (!config.getExcludedToolsList().contains("reckoner")) {
      t.addTool(new Reckoner1_0Tool());
    }
    return t;
  }
  
  public static void scan(Config config, SLProgressMonitor mon, boolean runRemotely) {
    LOG.info("Excluded: "+config.getExcludedToolsList());
    
    final ITool t = ToolUtil.create(config, runRemotely);                           
    LOG.info("Java version: "+config.getJavaVersion());
    LOG.info("Rules file: "+config.getPmdRulesFile());
  
    IToolInstance ti = t.create(config, mon);     
    LOG.info("Created "+ti.getClass().getSimpleName());
    ti.run();
  }
  
  public static String getTimeStamp() {
    Date date = Calendar.getInstance().getTime();
    long time = Calendar.getInstance().getTimeInMillis();

    /*
     * Change the colon on date to semi-colon as file name with a colon is
     * invalid
     */
    String timeStamp = date.toString().replace(":", ";") + " - "
        + String.valueOf(time);
    return timeStamp;
  }
}
