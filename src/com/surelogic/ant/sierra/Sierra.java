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
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author ethan
 * 
 */
public class Sierra extends Task {
	private String serverURL = null;
	private File resultsDir = null;
	private Project project = null;
	private boolean clean = false;
	private Tools tools = null;
	private File srcdir = null;
	private File bindir = null;

	/**
	 * @see Task
	 */
	public void execute() {
		validateParameters();
		runTools();
		parseResults();
		generateWSDL();
		if (serverURL != null) {
			uploadWSDL();
		}
		cleanup();
	}

	/**
	 * Runs the tools
	 */
	private void runTools() {

	}

	/**
	 * Parses the tools' results files into the SIERRA database
	 */
	private void parseResults() {

	}

	/**
	 * Generates a WSDL file from the updated database
	 */
	private void generateWSDL() {

	}

	/**
	 * Optional action. Uploads the generated WSDL file to the desired server.
	 */
	private void uploadWSDL() {

	}

	/**
	 * Cleans up after the task
	 */
	private void cleanup() {

	}

	/**
	 * Ensures all properties/parameters are set and valid If any parameters are
	 * invalid, this method will throw a BuildException
	 */
	public void validateParameters() {

		if (project == null) {
			throw new BuildException(
					"No projects were defined. The <projects> sub-tag is required.");
		} else {
			project.validate();
		}

		if (tools == null) {
			throw new BuildException(
					"No tools sub-tag was defined. The <tools> sub-tag is required.");
		} else {
			tools.validate();
		}

	}

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
	 * A collection for information pertaining to the tools (i.e., FindBugs,
	 * PMD)
	 * 
	 * @author ethan
	 * 
	 */
	public class Tools {
		private File baseDir = null;
		private String jdkVersion = null;
		private File resultsDir = new File(System.getProperty("java.io.tmpdir"));

		public void validate() {
			if (baseDir == null) {
				throw new BuildException(
						"No baseDir was set for the <tools> sub-tag. This is required.");
			} else {
				if (!baseDir.isDirectory()) {
					throw new BuildException(
							"The file specified by the baseDir parameter must be a valid directory.");
				}
				// TODO Ensure this is the proper directory - i.e. it contains
				// our tools
			}

			if (jdkVersion == null) {
				throw new BuildException(
						"The jdkVersion parameter is required.");
			} else {
				// TODO Ensure that the string adhers to a valid version number
				// format, i.e., xx.xx.xx
			}

			if (!resultsDir.isDirectory()) {
				throw new BuildException(
						"The parameter, resultsDir, must be a valid directory. "
								+ resultsDir.getAbsolutePath()
								+ " is not valid.");
			}
		}

		/**
		 * @return the baseDir
		 */
		public final File getBaseDir() {
			return baseDir;
		}

		/**
		 * @param baseDir
		 *            the baseDir to set
		 */
		public final void setBaseDir(File baseDir) {
			this.baseDir = baseDir;
		}

		/**
		 * @return the jdkVersion
		 */
		public final String getJdkVersion() {
			return jdkVersion;
		}

		/**
		 * @param jdkVersion
		 *            the jdkVersion to set
		 */
		public final void setJdkVersion(String jdkVersion) {
			this.jdkVersion = jdkVersion;
		}

		/**
		 * @return the resultsDir
		 */
		public final File getResultsDir() {
			return resultsDir;
		}

		/**
		 * @param resultsDir
		 *            the resultsDir to set
		 */
		public final void setResultsDir(File resultsDir) {
			this.resultsDir = resultsDir;
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
		private File baseDir = null;
		private Source source = null;
		private Binary binary = null;

		public void validate() {

			if (name == null) {
				throw new BuildException(
						"Parameter 'name' is required for project.");
			}
			if (baseDir == null ){
				throw new BuildException(
						"Parameter 'baseDir' is required if it is not defined for the enclosing <projects> tag.");
			} else if (baseDir != null) {
				if (!baseDir.isDirectory()) {
					throw new BuildException(
							"Parameter 'baseDir' must be a valid directory. "
									+ baseDir.getAbsolutePath()
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
			return baseDir;
		}

		public final void setBaseDir(File baseDir) {
			this.baseDir = baseDir;
		}
		
		public void addConfiguredSource(Source src){
			this.source = src;
		}

		public void addConfiguredBinary(Binary bin){
			this.binary = bin;
		}
		
		public Source getSource(){
			return source;
		}
		
		public Binary getBinary(){
			return binary;
		}
	}
	
	public class Source{
		
	}
	
	public class Binary{
		
	}

}
