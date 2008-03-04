package com.surelogic.sierra.tool.message;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.targets.IToolTarget;
import com.surelogic.sierra.tool.targets.ToolTarget;

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
	protected static final Logger LOG = SLLogger.getLogger("sierra");
	private String project = null;
	private List<String> timeseries = null;
	private String javaVersion = null;
	private String javaVendor = null;
	private Date runDateTime = null;
	private File baseDirectory = null;
	private File toolsDirectory = null;
	private String testCode = null;
	
	/**
	 * In MB
	 */
	private int memorySize = 1024;
	
	// Map from plugin id to their locations
	private final Map<String,String> pluginDirs = new HashMap<String,String>();

	// directory to store tool output in
	private File destDirectory = null;

	// Comma-separated list of tool names that won't be run
	private String excludedToolsList = null;

	// The full path and name of the run document
	private File scanDocument = null;

	// True if the temp directory inside the destDir should be deleted when done
	private boolean cleanTempFiles = false;

	// Path string containing the classpath for Sierra client, the Ant task and
	// Tools
	private String classpath = null;

	// File object for the PMD rules file
	private File pmdRulesFile = null;

	// Whether the tools are run in multiple threads
	private boolean multithreaded = false;
	
	private List<URI> paths = new ArrayList<URI>();

	private List<ToolTarget> targets = new ArrayList<ToolTarget>();
	private Set<ToolTarget> targetsAdded = null;
	
	public Config() {
		// Nothing to do
	}

	public String getProject() {
		return project;
	}

	public List<String> getTimeseries() {
		return timeseries;
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

	public int getMemorySize() {
	  return memorySize;
	}
	
	public void setMemorySize(int size) {
	  memorySize = (size > 0) ? size : 1024;
	}
	
	public String getTestCode() {
	  return testCode;
	}

	public void setTestCode(String code) {
	  testCode = code;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void setTimeseries(List<String> timeseries) {
		this.timeseries = timeseries;
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
	
	public Map<String,String> getPluginDirs() {
    return pluginDirs;
  }
	
	public void setPluginDirs(Map<String,String> dirs) {
	  this.pluginDirs.clear();
	  this.pluginDirs.putAll(dirs);
	}

	public void putPluginDir(String id, String location) {
	  pluginDirs.put(id, location);
	}
	
	public String getPluginDir(String id) {
	  return getPluginDir(id, true);
	}
	
	public String getPluginDir(String id, boolean required) {
	  String loc = pluginDirs.get(id);
	  if (required && loc == null) {
	    LOG.warning("No location for "+id);		
	    return null;
	  }
	  return loc;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseDirectory == null) ? 0 : baseDirectory.hashCode());
		result = prime * result
				+ ((classpath == null) ? 0 : classpath.hashCode());
		result = prime * result + (cleanTempFiles ? 1231 : 1237);
		result = prime * result
				+ ((destDirectory == null) ? 0 : destDirectory.hashCode());
		result = prime
				* result
				+ ((excludedToolsList == null) ? 0 : excludedToolsList
						.hashCode());
		result = prime * result
				+ ((javaVendor == null) ? 0 : javaVendor.hashCode());
		result = prime * result
				+ ((javaVersion == null) ? 0 : javaVersion.hashCode());
		result = prime * result + (multithreaded ? 1231 : 1237);
		result = prime * result
				+ ((pmdRulesFile == null) ? 0 : pmdRulesFile.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result
				+ ((timeseries == null) ? 0 : timeseries.hashCode());
		result = prime * result
				+ ((runDateTime == null) ? 0 : runDateTime.hashCode());
		result = prime * result
				+ ((scanDocument == null) ? 0 : scanDocument.hashCode());
		result = prime * result
				+ ((toolsDirectory == null) ? 0 : toolsDirectory.hashCode());
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
		if (baseDirectory == null) {
			if (other.baseDirectory != null)
				return false;
		} else if (!baseDirectory.equals(other.baseDirectory))
			return false;
		if (classpath == null) {
			if (other.classpath != null)
				return false;
		} else if (!classpath.equals(other.classpath))
			return false;
		if (cleanTempFiles != other.cleanTempFiles)
			return false;
		if (destDirectory == null) {
			if (other.destDirectory != null)
				return false;
		} else if (!destDirectory.equals(other.destDirectory))
			return false;
		if (excludedToolsList == null) {
			if (other.excludedToolsList != null)
				return false;
		} else if (!excludedToolsList.equals(other.excludedToolsList))
			return false;
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
		if (multithreaded != other.multithreaded)
			return false;
		if (pmdRulesFile == null) {
			if (other.pmdRulesFile != null)
				return false;
		} else if (!pmdRulesFile.equals(other.pmdRulesFile))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (timeseries == null) {
			if (other.timeseries != null)
				return false;
		} else if (!timeseries.equals(other.timeseries))
			return false;
		if (runDateTime == null) {
			if (other.runDateTime != null)
				return false;
		} else if (!runDateTime.equals(other.runDateTime))
			return false;
		if (scanDocument == null) {
			if (other.scanDocument != null)
				return false;
		} else if (!scanDocument.equals(other.scanDocument))
			return false;

		if (toolsDirectory == null) {
			if (other.toolsDirectory != null)
				return false;
		} else if (!toolsDirectory.equals(other.toolsDirectory))
			return false;
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

	public void addToClassPath(URI path) {
	  paths.add(path);
	}
	
	public void setPaths(List<URI> p) {
	  paths = p;
	}
	
	public List<URI> getPaths() {
	  return paths;
	}
	
	public void addTarget(ToolTarget t) {
	  if (t.getType() == IToolTarget.Type.AUX) {
	    if (targetsAdded == null) {
	      targetsAdded = new HashSet<ToolTarget>();
	    }
	    else if (targetsAdded.contains(t)) {
	      return;
	    }
	    targetsAdded.add(t);
	  }
	  targets.add(t);
	}
	
	public void setTargets(List<ToolTarget> t) {
	  targets = t;  
	}
	
	public List<ToolTarget> getTargets() {
	  return targets;
	}

  public boolean hasNothingToScan() {
    for(ToolTarget t : targets) {
      switch (t.getType()) {
        case BINARY:
        case SOURCE:
          return false;
        default:
          continue;
      }
    }
    return true;
  }
}
