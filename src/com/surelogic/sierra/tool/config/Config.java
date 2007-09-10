package com.surelogic.sierra.tool.config;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The config object for the run document.
 * 
 * @author Tanmay.Sinha
 * 
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class Config {

	private String project = null;
	private List<String> qualifiers = null;
	private String javaVersion = null;
	private String javaVendor = null;
	private Date runDateTime = null;
	private File baseDirectory = null;
	private File toolsDirectory = null;
	// directory to store tool output in
	private File destDirectory = null;
	// Comma-separated list of tool names that won't be run
	private String excludedToolsList = null;
	// The full path and name of the run document
	private File scanDocument = null;
	// True if the temp directory inside the destDir should be deleted when done
	private boolean cleanTempFiles = false;
	// Path string containing all source directories to be scanned
	private String sourceDirs = null;
	// Path string containing all binary directories to be scanned
	private String binDirs = null;
	// Path string containing the classpath for Sierra client, the Ant task and
	// Tools
	private String classpath = null;
	// File object for the PMD rules file
	private File pmdRulesFile = null;

	// Whether the tools are run in multiple threads
	private boolean multithreaded = false;

	public Config() {
		// Nothing to do
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

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File getToolsDirectory() {
		return toolsDirectory;
	}

	public void setToolsDirectory(File toolsDirectory) {
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

		if (javaVendor == null && other.javaVendor != null) {
			return false;
		} else if (!javaVendor.equals(other.javaVendor)) {
			return false;
		} else if (javaVersion == null && other.javaVersion != null) {
			return false;
		} else if (!javaVersion.equals(other.javaVersion)) {
			return false;
		} else if (project == null && other.project != null) {
			return false;
		} else if (!project.equals(other.project)) {
			return false;
		} else if (qualifiers == null && other.qualifiers != null) {
			return false;
		} else if (!qualifiers.equals(other.qualifiers)) {
			return false;
		} else if (runDateTime == null && other.runDateTime != null) {
			return false;
		} else if (!runDateTime.equals(other.runDateTime)) {
			return false;
		} else if (baseDirectory == null & other.baseDirectory != null) {
			return false;
		} else if (!baseDirectory.equals(other.baseDirectory)) {
			return false;
		} else if (toolsDirectory == null && other.toolsDirectory != null) {
			return false;
		} else if (!toolsDirectory.equals(other.toolsDirectory)) {
			return false;
		} else if (destDirectory == null && other.toolsDirectory != null) {
			return false;
		} else if (!destDirectory.equals(other.getDestDirectory())) {
			return false;
		} else if (excludedToolsList == null
				&& other.getExcludedToolsList() != null) {
			return false;
		} else if (!excludedToolsList.equals(other.getExcludedToolsList())) {
			return false;
		} else if (scanDocument == null && other.getScanDocument() != null) {
			return false;
		} else if (!scanDocument.equals(other.getScanDocument())) {
			return false;
		} else if (cleanTempFiles != other.isCleanTempFiles()) {
			return false;
		} else if (sourceDirs == null && other.getSourceDirs() != null) {
			return false;
		} else if (!sourceDirs.equals(other.getSourceDirs())) {
			return false;
		} else if (binDirs == null && other.getBinDirs() != null) {
			return false;
		} else if (!binDirs.equals(other.getBinDirs())) {
			return false;
		} else if (classpath == null && other.getClasspath() != null) {
			return false;
		} else if (!classpath.equals(other.getClasspath())) {
			return false;
		} else if (pmdRulesFile == null && other.getPmdRulesFile() != null) {
			return false;
		} else if (!pmdRulesFile.equals(other.getPmdRulesFile())) {
			return false;
		} else if (multithreaded != other.isMultithreaded()) {
			return false;
		}

		return true;
	}

	/**
	 * @return the destDirectory
	 */
	public final File getDestDirectory() {
		return destDirectory;
	}

	/**
	 * @param destDirectory
	 *            the destDirectory to set
	 */
	public final void setDestDirectory(File destDirectory) {
		this.destDirectory = destDirectory;
	}

	/**
	 * @return the excludedToolsList
	 */
	public final String getExcludedToolsList() {
		return excludedToolsList;
	}

	/**
	 * @param excludedToolsList
	 *            the excludedToolsList to set
	 */
	public final void setExcludedToolsList(String excludedToolsList) {
		this.excludedToolsList = excludedToolsList;
	}

	/**
	 * @return the runDocument
	 */
	public final File getScanDocument() {
		return scanDocument;
	}

	/**
	 * @param runDocumentName
	 *            the runDocumentName to set
	 */
	public final void setScanDocument(File scanDocument) {
		this.scanDocument = scanDocument;
	}

	/**
	 * @return the cleanTempFiles
	 */
	public final boolean isCleanTempFiles() {
		return cleanTempFiles;
	}

	/**
	 * @param cleanTempFiles
	 *            the cleanTempFiles to set
	 */
	public final void setCleanTempFiles(boolean cleanTempFiles) {
		this.cleanTempFiles = cleanTempFiles;
	}

	/**
	 * @return the sourceDirs
	 */
	public final String getSourceDirs() {
		return sourceDirs;
	}

	/**
	 * @param sourceDirs
	 *            the sourceDirs to set
	 */
	public final void setSourceDirs(String sourceDirs) {
		this.sourceDirs = sourceDirs;
	}

	/**
	 * @return the binDirs
	 */
	public final String getBinDirs() {
		return binDirs;
	}

	/**
	 * @param binDirs
	 *            the binDirs to set
	 */
	public final void setBinDirs(String binDirs) {
		this.binDirs = binDirs;
	}

	/**
	 * @return the classpath
	 */
	public final String getClasspath() {
		return classpath;
	}

	/**
	 * @param classpath
	 *            the classpath to set
	 */
	public final void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	/**
	 * @return the pmdRulesFile
	 */
	public final File getPmdRulesFile() {
		return pmdRulesFile;
	}

	/**
	 * @param pmdRulesFile
	 *            the pmdRulesFile to set
	 */
	public final void setPmdRulesFile(File pmdRulesFile) {
		this.pmdRulesFile = pmdRulesFile;
	}

	/**
	 * @return the multithreaded
	 */
	public final boolean isMultithreaded() {
		return multithreaded;
	}

	/**
	 * @param multithreaded
	 *            the multithreaded to set
	 */
	public final void setMultithreaded(boolean multithreaded) {
		this.multithreaded = multithreaded;
	}

}
