package com.surelogic.sierra.tool;

import java.util.Calendar;
import java.util.Date;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.findbugs.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.pmd.*;
import com.surelogic.sierra.tool.reckoner.*;

public class ToolUtil {
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
      t.addTool(new FindBugs1_3_0Tool(config.getToolsDirectory()));
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
    System.out.println("Excluded: "+config.getExcludedToolsList());
    
    final ITool t = ToolUtil.create(config, runRemotely);                           
    System.out.println("Java version: "+config.getJavaVersion());
    System.out.println("Rules file: "+config.getPmdRulesFile());
  
    IToolInstance ti = t.create(config, mon);                         
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
