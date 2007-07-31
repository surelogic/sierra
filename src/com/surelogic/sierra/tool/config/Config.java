package com.surelogic.sierra.tool.config;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The config object for the run document.
 * 
 * @author Tanmay.Sinha
 * 
 */
@XmlType
@XmlRootElement
public class Config {

	private String project;
	private List<String> qualifiers;
	private String javaVersion;
	private String javaVendor;
	private Date runDateTime;
	private String baseDirectory;
	private String toolsDirectory;

	public Config() {
		// Nothing to do
	}

	// TODO: Fix this to map to the ANT lauch config
	public Config(BaseConfig baseconfig) {
		// this.runDateTime = baseconfig.runDateTime;
		this.project = baseconfig.getProjectName();

		// FIX THIS: HARDCODED
		this.javaVendor = "Sun";

		this.javaVersion = baseconfig.getJdkVersion();
		this.baseDirectory = baseconfig.getBaseDirectory();
		this.toolsDirectory = baseconfig.getToolsDirectory();
		// this.qualifiers = Collections.unmodifiableList(builder.qualifiers);
	}

	public String getProject() {
		return project;
	}

	public List<String> getQualifiers() {
		return qualifiers;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public Date getRunDateTime() {
		return runDateTime;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void setQualifiers(List<String> qualifiers) {
		this.qualifiers = qualifiers;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public void setRunDateTime(Date runDateTime) {
		this.runDateTime = runDateTime;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getToolsDirectory() {
		return toolsDirectory;
	}

	public void setToolsDirectory(String toolsDirectory) {
		this.toolsDirectory = toolsDirectory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((javaVendor == null) ? 0 : javaVendor.hashCode());
		result = prime * result
				+ ((javaVersion == null) ? 0 : javaVersion.hashCode());
		result = prime * result
				+ ((baseDirectory == null) ? 0 : baseDirectory.hashCode());
		result = prime * result
				+ ((toolsDirectory == null) ? 0 : toolsDirectory.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result
				+ ((qualifiers == null) ? 0 : qualifiers.hashCode());
		result = prime * result
				+ ((runDateTime == null) ? 0 : runDateTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Config other = (Config) obj;
		if (javaVendor == null) {
			if (other.javaVendor != null)
				return false;
		} else if (!javaVendor.equals(other.javaVendor))
			return false;
		if (javaVersion == null) {
			if (other.javaVersion != null)
				return false;
		} else if (!javaVersion.equals(other.javaVersion))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (qualifiers == null) {
			if (other.qualifiers != null)
				return false;
		} else if (!qualifiers.equals(other.qualifiers))
			return false;
		if (runDateTime == null) {
			if (other.runDateTime != null)
				return false;
		} else if (!runDateTime.equals(other.runDateTime))
			return false;
		if (baseDirectory == null) {
			if (other.baseDirectory != null)
				return false;
		} else if (!baseDirectory.equals(other.baseDirectory))
			return false;
		if (toolsDirectory == null) {
			if (other.toolsDirectory != null)
				return false;
		} else if (!toolsDirectory.equals(other.toolsDirectory))
			return false;
		return true;
	}

}
