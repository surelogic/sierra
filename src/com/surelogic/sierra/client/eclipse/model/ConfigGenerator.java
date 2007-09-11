package com.surelogic.sierra.client.eclipse.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.surelogic.sierra.client.eclipse.preferences.PreferenceConstants;
import com.surelogic.sierra.tool.SierraConstants;
import com.surelogic.sierra.tool.analyzer.BuildFileGenerator;
import com.surelogic.sierra.tool.config.Config;

public final class ConfigGenerator {

	private static final ConfigGenerator INSTANCE = new ConfigGenerator();
	/** The location to store tool results */
	private final File f_resultRoot = new File(
			SierraConstants.SIERRA_RESULTS_PATH);

	/** The default folder from the preference page */
	private final String f_sierraPath = PreferenceConstants.getSierraPath();

	/** The plug-in directory that has tools folder */
	private final String tools = BuildFileGenerator.getToolsDirectory()
			+ SierraConstants.TOOLS_FOLDER;

	private ConfigGenerator() {
		// singleton
	}

	public static ConfigGenerator getInstance() {
		return INSTANCE;
	}

	public List<Config> getConfigs(List<IJavaProject> projects) {

		List<Config> configs = new ArrayList<Config>();

		for (IJavaProject p : projects) {
			configs.add(getConfig(p));
		}

		return configs;

	}

	private Config getConfig(IJavaProject project) {
		String projectPath = project.getResource().getLocation().toString();
		File baseDir = new File(projectPath);
		File scanDocument = new File(f_sierraPath + File.separator
				+ project.getProject().getName() + " - " + getTimeStamp()
				+ SierraConstants.PARSED_FILE_SUFFIX);

		Config config = new Config();

		config.setBaseDirectory(baseDir);
		config.setProject(project.getProject().getName());
		config.setDestDirectory(f_resultRoot);
		config.setScanDocument(scanDocument);
		config.setJavaVendor(System.getProperty("java.vendor"));
		config.setScanDocument(scanDocument);
		config.setToolsDirectory(new File(tools));

		// Get clean option
		// Get excluded tools
		// Get included dirs -project specific
		// Get excluded dirs - project specific

		return config;
	}

	private String getTimeStamp() {
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
