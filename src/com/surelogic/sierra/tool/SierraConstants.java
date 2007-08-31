package com.surelogic.sierra.tool;

import java.io.File;

public final class SierraConstants {

	/** The location of tools folder */
	public static final String TOOLS_FOLDER = "Tools";

	/** The default location for storing results */
	public static final String SIERRA_RESULTS = ".SierraResults";

	/** The default extension for run document */
	public static final String PARSED_FILE_SUFFIX = ".sierra";

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
	public static final String BUC_LIB_LOCATION = "Tools/backport-util-concurrent-3.0";

	/** JAXB libraries location */
	public static final String JAX_LIB_LOCATION = "Tools/jax-ws";

	/** The name of build file for multiple projects */
	@Deprecated
	public static final String MULTIPLE_SIERRA_PROJECTS = "multiple-sierra-projects.xml";

	/** Default priority value for database */
	public static final String PRIORITY = "priority";

	/** The default name of package for files in root folder */
	public static final String DEFAULT_PACKAGE = "Default Package";
}
