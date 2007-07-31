/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import java.util.Arrays;

import org.apache.tools.ant.BuildException;

/**
 * A collection for information pertaining to the tools (i.e., FindBugs, PMD)
 * 
 * @author ethan
 * 
 */
public class Tools {
	private final static String DEFAULT_PMD_JAVA_VERSION = "1.5";
	
	private PmdConfig pmdConfig = null;
	private String[] exclude = new String[0];;

	public void validate() {
		// TODO should validate the tool names against valid tools
		if (exclude != null) {
			for (String tool : exclude) {
				if (Arrays.binarySearch(SierraAnalysis.toolList, tool) < 0) {
					StringBuffer buf = new StringBuffer();
					buf.append(tool);
					buf
							.append(" is not a valid tool name. Valid tool names are: \n");
					for (String toolName : SierraAnalysis.toolList) {
						buf.append(toolName);
						buf.append("\n");
					}
					throw new BuildException(buf.toString());
				}
			}
		}
		if (pmdConfig == null) {
			pmdConfig = new PmdConfig();
			pmdConfig.setJavaVersion(DEFAULT_PMD_JAVA_VERSION);
		} else {
			pmdConfig.validate();
		}
	}

	public void setExclude(String list) {
		exclude = list.split(",");
		for (int i = 0; i < exclude.length; i++) {
			exclude[i] = exclude[i].trim().toLowerCase();
		}
	}

	public String[] getExclude() {
		return exclude;
	}

	public void addConfiguredPmdConfig(PmdConfig config) {
		this.pmdConfig = config;
	}

	public PmdConfig getPmdConfig() {
		return pmdConfig;
	}

	/**
	 * Represents a configuration attribute for the PMD tool
	 * 
	 * @author ethan
	 * 
	 */
	public static class PmdConfig {
		private String javaVersion = null;

		public void validate() {
			if (!javaVersion.matches("\\d\\.\\d")) {
				throw new BuildException(
						"Invalid version string for pmdconfig's 'javaVersion' attribute. Must be one of the following: 1.3, 1.4, 1.5, 1.6 ");
			}
		}

		public void setJavaVersion(String version) {
			this.javaVersion = version;
		}

		public String getJavaVersion() {
			return javaVersion;
		}
	}
}
