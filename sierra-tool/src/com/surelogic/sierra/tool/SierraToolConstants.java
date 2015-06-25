package com.surelogic.sierra.tool;

import java.io.File;
import java.util.*;

public final class SierraToolConstants {

  /**
   * If running in the client, these reference some plug-in identifiers that
   * have no activator
   */
  public static final String COMMON_CORE_PLUGIN_ID = "com.surelogic.common.core";
  public static final String MESSAGE_PLUGIN_ID = "com.surelogic.sierra.message";
  public static final String TOOL_PLUGIN_ID = "com.surelogic.sierra.tool";
  public static final String PMD_PLUGIN_ID = "com.surelogic.sierra.pmd";
  public static final String FB_PLUGIN_ID = "com.surelogic.sierra.fb";

  /**
   * Java property for the aux path file
   */
  public static final String AUX_PATH_PROPERTY = "surelogic.aux.path.file";

  /**
   * Java property for the location of the config file
   */
  public static final String CONFIG_PROPERTY = "surelogic.config";

  /** The location of tools folder */
  public static final String TOOLS_FOLDER = "Tools";

  /** The default location for storing results */
  public static final String SIERRA_RESULTS = ".SierraResults";

  /** The default extension for run document */
  public static final String PARSED_FILE_SUFFIX = ".sierra.gz";

  /** The default extension for zipped run document */
  public static final String PARSED_ZIP_FILE_SUFFIX = ".sierra.zip";

  public static final List<String> PARSED_FILE_SUFFIXES;
  static {
    final List<String> temp = new ArrayList<>(2);
    temp.add(PARSED_ZIP_FILE_SUFFIX);
    temp.add(PARSED_FILE_SUFFIX);
    PARSED_FILE_SUFFIXES = Collections.unmodifiableList(temp);
  }

  public static final boolean RUN_TOGETHER = true;
  public static final boolean USE_ZIP = false || !RUN_TOGETHER;
  public static final boolean CREATE_ZIP_DIRECTLY = RUN_TOGETHER && false;

  /** The complete path for the SierraResults folder */
  public static final String SIERRA_RESULTS_PATH = System.getProperty("java.io.tmpdir") + File.separator + SIERRA_RESULTS;

  /**
   * Possible tool messages
   */
  public static final int ERROR_CREATING_AUX_PATH = 66;
  public static final int ERROR_CREATING_CONFIG = 67;
}
