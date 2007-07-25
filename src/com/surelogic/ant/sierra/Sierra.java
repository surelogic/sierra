/**
 * 
<sierra-analysis  
    resultsdir="/tool/results/output/dir"
    srcdir="/source/directory"  
    bindir="/path/to/.class/files"
    serverURL=" http://server.url"  
    clean="true">

    <project  name="project"  dir="/project/directory">
        <source  path="source dir">  
            <include  name="package/p1/**"/>
            <exclude  name="package/p1/** /test* />
        </source>

        <binary  path="binary  dir">
            <include  name="package/p1/**"/>  
            <exclude  name="package/p1/** /test* />
        </binary>       
    </project>

    <tools  exclude="FindBugs">
        <pmd-config javaVersion=Ó1.5Ó/>
    </tools>
</sierra-analysis>

The following attributes are optional:
resultsdir - defaults to /tmp
Srcdir - required unless nested <source> elements are defined
BinDir - required unless nested <binary> elements are defined
ServerURL - if set, will attempt to send the WSDL file
Clean - if set to true, will clean the tool result files and the WSDL (if it
was sent to the server successfully)

Source -multiple can be defined
    - include - same setup as in most ant tasks
    - exclude - ditto

Binary - multiple can be defined
    - if this is not defined, it will use the source settings

Tools - if not defined, will run all of the tools

The tools' jar files will be included in the <taskdef> that loads this
task's jar.
 *
 */
package com.surelogic.ant.sierra;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author ethan
 * 
 */
public class Sierra extends Task {
	private org.apache.tools.ant.Project proj = getProject();
	
	// Optional attribute, if present, we send the WSDL file to this server
	private String serverURL = null; 										
	
	// Optional, if omitted, the system's tmp folder is used
	private File destDir= new File(System.getProperty("java.io.tmp")); 		
	
	// Required
	private Project project = null; 	
	
	// Optional, defaults to false
	private boolean clean = false; 		
	
    // Optional, if omitted, all tools are run and PMD's java version is set to 1.5
	private Tools tools = null;    		
	
    // Optional
	private File srcdir = null;			
	
    // Optional
	private File bindir = null;			
	
	/* *********************** CONSTANTS *******************************/
	private final static String[] toolList = new String[]{"findbugs", "pmd"};
	private static final String DEFAULT_PMD_JAVA_VERSION = "1.5";
	
	static{
		Arrays.sort(toolList);
	}

	/**
	 * @see Task
	 */
	public void execute() {
		validateParameters();
		runTools();
		generateWSDL();
		if (serverURL != null) {
			uploadWSDL();
		}
		
		if (clean) {
			cleanup();
		}
	}

	/**
	 * Runs the tools
	 */
	private void runTools() {
		log("Running tools...", org.apache.tools.ant.Project.MSG_INFO);
	}

	/**
	 * Generates a WSDL file from the updated database
	 */
	private void generateWSDL() {
		log("Generating the WSDL file...", org.apache.tools.ant.Project.MSG_INFO);
	}

	/**
	 * Optional action. Uploads the generated WSDL file to the desired server.
	 */
	private void uploadWSDL() {
		log("Uploading the WSDL file to " + serverURL + "...", org.apache.tools.ant.Project.MSG_INFO);
	}

	/**
	 * Cleans up after the task
	 */
	private void cleanup() {
		log("Cleaning up...", org.apache.tools.ant.Project.MSG_INFO);
	}

	/**
	 * Ensures all properties/parameters are set and valid If any parameters are
	 * invalid, this method will throw a BuildException
	 */
	public void validateParameters() {
		if(destDir != null && !destDir.isDirectory()){
			throw new BuildException(
					"'destdir' must be a valid directory.");
		}
		
		if(srcdir == null){
			// TODO ensure that there are source directories defined later
		}
		
		if (bindir == null){
			// TODO ensure that there are binary directories defined
		}

		if (project == null) {
			throw new BuildException(
					"No project was defined. The <projects> sub-tag is required.");
		} else {
			project.validate();
		}

		if (tools != null) {
			tools.validate();
		}

	}

	/**
	 * A collection for information pertaining to the tools (i.e., FindBugs,
	 * PMD)
	 * 
	 * @author ethan
	 * 
	 */
	public class Tools {
		private PmdConfig pmdConfig = null;
		private String[] exclude = null;

		public void validate() {
			if(exclude != null){
				for (String tool : exclude) {
					if(Arrays.binarySearch(toolList, tool.toLowerCase()) < 0){
						StringBuffer buf = new StringBuffer();
						buf.append(tool);
						buf.append(" is not a valid tool name. Valid tool names are: \n");
						for (String toolName : toolList) {
							buf.append(toolName);
							buf.append("\n");
						}
        				throw new BuildException(buf.toString());
					}
				}
			}
			if(pmdConfig == null){
				pmdConfig = new PmdConfig();
				pmdConfig.setJavaVersion(DEFAULT_PMD_JAVA_VERSION);
			}
			else{
				pmdConfig.validate();
			}
		}
		
		public void setExclude(String list){
			exclude = list.split(",");
			for (int i = 0; i < exclude.length; i++){
				exclude[i] = exclude[i].trim();
			}
		}
		
		public String[] getExclude(){
			return exclude;
		}
		
		public void addConfiguredPmdConfig(PmdConfig config){
			this.pmdConfig = config;
		}

		public PmdConfig getPmdConfig(){
			return pmdConfig;
		}
	}
	
	/**
	 * Represents a configuration attribute for the PMD tool
	 * @author ethan
	 *
	 */
	public class PmdConfig {
		private String javaVersion = null;
		
		public void validate(){
			if(!javaVersion.matches("\\d+\\.\\d+(\\.\\d+)*")){
				throw new BuildException(
					"Invalid version string for pmdconfig's 'javaVersion' attribute. Must be of the form: XX.XX.XX where X is a number, such as 1.4.2.");
			}
		}
		
		public void setJavaVersion(String version){
			this.javaVersion = version;
		}
		
		public String getJavaVersion(){
			return javaVersion;
		}
	}

	/**
	 * Class representing the definition of a single project
	 * 
	 * @author ethan
	 * 
	 */
	public class Project {
		private String name = null;

		// If set, this will override the baseDir value set in the Projects
		private File dir = null;
		private Vector<Source> sources = new Vector<Source>();
		private Vector<Binary> binaries = new Vector<Binary>();

		public void validate() {

			if (name == null) {
				throw new BuildException(
						"Parameter 'name' is required for 'project'.");
			}
			if (dir == null) {
				throw new BuildException(
						"Parameter 'baseDir' is required if it is not defined for the enclosing <projects> tag.");
			} else if (dir != null) {
				if (!dir.isDirectory()) {
					throw new BuildException(
							"Parameter 'baseDir' must be a valid directory. "
									+ dir.getAbsolutePath()
									+ " is not a valid directory.");
				}
			}
		}

		public final String getName() {
			return name;
		}

		public final void setName(String name) {
			this.name = name;
		}

		public final File getBaseDir() {
			return dir;
		}

		public final void setBaseDir(File baseDir) {
			this.dir = baseDir;
		}

		public void addConfiguredSource(Source src) {
			sources.add(src);
		}

		public void addConfiguredBinary(Binary bin) {
			binaries.add(bin);
		}

		public Vector<Source> getSources() {
			return sources;
		}

		public Vector<Binary> getBinaries() {
			return binaries;
		}
	}

	public class Source {
		

	}

	public class Binary {

	}
	
	
	
	/******************************************************
	 * Getters and Setters for attributes
	 ******************************************************/
	
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void addConfiguredTools(Tools tools) {
		this.tools = tools;
	}


	/**
	 * @return the destDir
	 */
	public final File getDestDir() {
		return destDir;
	}

	/**
	 * @param destDir the destDir to set
	 */
	public final void setDestDir(File destDir) {
		this.destDir = destDir;
	}

	/**
	 * @return the clean
	 */
	public final boolean isClean() {
		return clean;
	}

	/**
	 * @param clean the clean to set
	 */
	public final void setClean(boolean clean) {
		this.clean = clean;
	}

	/**
	 * @return the srcdir
	 */
	public final File getSrcdir() {
		return srcdir;
	}

	/**
	 * @param srcdir the srcdir to set
	 */
	public final void setSrcdir(File srcdir) {
		this.srcdir = srcdir;
	}

	/**
	 * @return the bindir
	 */
	public final File getBindir() {
		return bindir;
	}

	/**
	 * @param bindir the bindir to set
	 */
	public final void setBindir(File bindir) {
		this.bindir = bindir;
	}

}
