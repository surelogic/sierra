package com.surelogic.sierra.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
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
  
  public static ITool create(Config config, boolean runRemotely) {
	final boolean debug = LOG.isLoggable(Level.FINE);
    if (runRemotely) {
      return new LocalTool(debug);
    }
    final MultiTool t = new MultiTool(true);
    if (!config.getExcludedToolsList().contains("findbugs")) {
      t.addTool(new FindBugs1_3_4Tool(config.getPluginDir(SierraToolConstants.FB_PLUGIN_ID), debug));
    }
    if (!config.getExcludedToolsList().contains("pmd")) {
      t.addTool(new PMD4_2_1Tool(debug));
      t.addTool(new CPD4_1Tool(debug));
    }
    if (!config.getExcludedToolsList().contains("reckoner")) {
      t.addTool(new Reckoner1_0Tool(debug));
    }
    return t;
  }
  
  public static void scan(Config config, SLProgressMonitor mon, boolean runRemotely) {    
    final boolean fineIsLoggable = LOG.isLoggable(Level.FINE);
    final ITool t = ToolUtil.create(config, runRemotely);                           
    
    if (fineIsLoggable) {
      LOG.fine("Excluded: "+config.getExcludedToolsList());
      LOG.fine("Java version: "+config.getJavaVersion());
      LOG.fine("Rules file: "+config.getPmdRulesFile());
    }
    IToolInstance ti = t.create(config, mon);     
    if (fineIsLoggable) {
      LOG.fine("Created "+ti.getClass().getSimpleName());
    }
    ti.run();
  }
  
  public static String getTimeStamp() {
    final Date date = Calendar.getInstance().getTime();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-'at'-HH.mm.ss.SSS");    
    return dateFormat.format(date);
  }
}
