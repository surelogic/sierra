package com.surelogic.sierra.tool;

import java.io.File;
import java.util.*;

public final class SierraToolConstants {

	/**
	 * If running in the client, these reference some plug-in identifiers that
	 * have no activator
	 */
	public static final String JUNIT_PLUGIN_ID = "org.junit";
	public static final String JUNIT4_PLUGIN_ID = "org.junit4";
	public static final String COMMON_PLUGIN_ID = "com.surelogic.common";
	public static final String MESSAGE_PLUGIN_ID = "com.surelogic.sierra.message";
	public static final String TOOL_PLUGIN_ID = "com.surelogic.sierra.tool";
	public static final String PMD_PLUGIN_ID = "com.surelogic.sierra.pmd";
	public static final String FB_PLUGIN_ID = "com.surelogic.sierra.fb";
	public static final String RECKONER_PLUGIN_ID = "com.surelogic.sierra.reckoner";
	public static final String JAVA5_PLUGIN_ID = "com.surelogic.sierra.java5.compatibility";
	public static final String JDT_CORE_PLUGIN_ID = "org.eclipse.jdt.core";
	public static final String CORE_RUNTIME_PLUGIN_ID = "org.eclipse.core.runtime";

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
		List<String> temp = new ArrayList<String>(2);
		temp.add(PARSED_ZIP_FILE_SUFFIX);
		temp.add(PARSED_FILE_SUFFIX);
		PARSED_FILE_SUFFIXES = Collections.unmodifiableList(temp);
	}
	
	public static final boolean RUN_TOGETHER = true;
	public static final boolean USE_ZIP = false || !RUN_TOGETHER;
	public static final boolean CREATE_ZIP_DIRECTLY = false && RUN_TOGETHER;

	/** The complete path for the SierraResults folder */
	public static final String SIERRA_RESULTS_PATH = System
			.getProperty("java.io.tmpdir")
			+ File.separator + SIERRA_RESULTS;

	// Constants for BuildFileGenerator project

	/** The name of sierra build file */
	public static final String SIERRA_BUILD_FILE = "sierra.xml";

	/** Location for Antlib for taskdef */
	public static final String ANTLIB_DIR = "com/surelogic/sierra/tool/ant/antlib.xml";

	/** Source tag */
	public static final String SIERRA_TOOL_SRC = "src";

	/** Include all expression */
	public static final String INCLUDE_ALL = "**/*";

	/** Include all jars expression */
	public static final String INCLUDE_ALL_JARS = "**/*.jar";

	/** Backport util concurrent libraries */
	public static final String BUC_LIB_LOCATION = "/Tools/backport-util-concurrent-3.0";

	/** JAXB libraries location */
	public static final String JAX_LIB_LOCATION = "/Tools/jax-ws";

	/** The tool property */
	public static final String TOOL_PROPERTY = "${tool}";

	/** Default priority value for database */
	public static final String PRIORITY = "priority";

	/**
	 * Possible tool messages
	 */
	public static final int ERROR_CREATING_AUX_PATH = 66;
	public static final int ERROR_CREATING_CONFIG = 67;
}
