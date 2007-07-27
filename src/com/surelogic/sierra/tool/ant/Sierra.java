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
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.RedirectorElement;
import org.apache.tools.ant.util.FileUtils;

/**
 * @author ethan
 * 
 */
public class Sierra extends Task {
	protected Redirector redirector = new Redirector(this);
	protected RedirectorElement redirectorElement;
	private Environment env = new Environment();
	private Long timeout = 300L;
	private CommandlineJava cmdl = new CommandlineJava();
	private static final FileUtils fileUtils = FileUtils.getFileUtils();

	private org.apache.tools.ant.Project proj = getProject();

	// Optional attribute, if present, we send the WSDL file to this server
	private String serverURL = null;

	// Optional, if omitted, the system's tmp folder is used
	private File destDir = new File(System.getProperty("java.io.tmpdir"));

	// Required
	private Project project = null;

	// Optional, defaults to false
	private boolean clean = false;

	// Optional, if omitted, all tools are run and PMD's java version is set to
	// 1.5
	private Tools tools = null;

	// Optional
	private Path srcdir = new Path(proj);

	// Optional
	private Path bindir = new Path(proj);

	/* *********************** CONSTANTS ****************************** */
	private final static String[] toolList = new String[] { "findbugs", "pmd" };
	private static final String DEFAULT_PMD_JAVA_VERSION = "1.5";

	static {
		Arrays.sort(toolList);
	}

	/**
	 * @see Task
	 */
	public void execute() {
		validateParameters();
		runTools();
		generateRunDocument();
		if (serverURL != null && !"".equals(serverURL)) {
			uploadRunDocument();
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
		log("Source path: " + srcdir, org.apache.tools.ant.Project.MSG_DEBUG);
		log("Binary path: " + bindir, org.apache.tools.ant.Project.MSG_DEBUG);
		log("Results will be saved to: " + destDir, org.apache.tools.ant.Project.MSG_DEBUG);
		/*
		 * 
		 * if (Arrays.binarySearch(tools.getExclude(), "pmd") < 0) { // run PMD
		 * try { fork(cmdl.getCommandline()); } catch (BuildException e) {
		 * log("Failed to start PMD process.", e,
		 * org.apache.tools.ant.Project.MSG_ERR); } }
		 * 
		 * if (Arrays.binarySearch(tools.getExclude(), "findbugs") < 0) { // run
		 * FindBugs try { fork(cmdl.getCommandline()); } catch (BuildException
		 * e) { log("Failed to start FindBugs process.", e,
		 * org.apache.tools.ant.Project.MSG_ERR); } }
		 */
	}

	/**
	 * Generates a WSDL file from the updated database
	 */
	private void generateRunDocument() {
		log("Generating the Run document...",
				org.apache.tools.ant.Project.MSG_INFO);
	}

	/**
	 * Optional action. Uploads the generated WSDL file to the desired server.
	 */
	private void uploadRunDocument() {
		log("Uploading the Run document to " + serverURL + "...",
				org.apache.tools.ant.Project.MSG_INFO);
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
		if (destDir != null && !destDir.isDirectory()) {
			throw new BuildException("'destdir' must be a valid directory.");
		}

		// Check this before the srcdir/bindir so that we know whether the
		// project object is null or not
		if (project == null) {
			throw new BuildException(
					"No project was defined. The <project> sub-tag is required.");
		} else {
			project.validate();
		}

		if (srcdir == null) {
			throw new BuildException(
					"Either 'srcdir' or 'sources' must be defined.");
		} else {
			// TODO run through the path and verify all directories are valid
		}

		if (bindir == null) {
			log("No value set for 'bindir' or 'binaries'. Values for 'srcdir' or 'sources' will be used.");
		} else {
			// TODO run through the path and verify all directories are valid
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
	public static class Tools {
		private PmdConfig pmdConfig = null;
		private String[] exclude = null;

		public void validate() {
			if (exclude != null) {
				for (String tool : exclude) {
					if (Arrays.binarySearch(toolList, tool) < 0) {
						StringBuffer buf = new StringBuffer();
						buf.append(tool);
						buf
								.append(" is not a valid tool name. Valid tool names are: \n");
						for (String toolName : toolList) {
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

	/**
	 * Class representing the definition of a single project
	 * 
	 * @author ethan
	 * 
	 */
	public static class Project {
		private String name = null;
		private File dir = null;
		private List<Source> sources = new ArrayList<Source>();
		private List<Binary> binaries = new ArrayList<Binary>();
		private Path src = null;
		private Path bin = null;
		private org.apache.tools.ant.Project proj = null;

		public Project(org.apache.tools.ant.Project proj) {
			this.proj = proj;
			src = new Path(proj);
			bin = new Path(proj);
		}

		public void validate() {

			if (name == null) {
				throw new BuildException(
						"Parameter 'name' is required for 'project'.");
			}
			if (dir == null) {
				throw new BuildException(
						"Parameter 'baseDir' is required for 'project'.");
			} else if (dir != null) {
				if (!dir.isDirectory()) {
					throw new BuildException(
							"Parameter 'dir' must be a valid directory. "
									+ dir.getAbsolutePath()
									+ " is not a valid directory.");
				}
			}
			if (!sources.isEmpty()) {
				for (Source source : sources) {
    				System.out.println("Sources in Project element: " + source.toString());//, org.apache.tools.ant.Project.MSG_DEBUG);
    				
					String[] list = source.getDirectoryScanner()
							.getIncludedDirectories();
					
					File basedir = source.getDir();
					
					for (String string : list) {
						File srcDir = fileUtils.resolveFile(basedir, string);
						if (!srcDir.exists()) {
							throw new BuildException("srcDir \""
									+ srcDir.getPath() + "\" does not exist.");
						}
						src.append(new Path(proj,
    						srcDir.getAbsolutePath()));
					}
				}
			}
			if (!binaries.isEmpty()) {
				for (Binary binary : binaries) {
    				System.out.println("Binaries in Project element: " + binary.toString());//, org.apache.tools.ant.Project.MSG_DEBUG);
    				
					String[] list = binary.getDirectoryScanner()
							.getIncludedDirectories();
					File basedir = binary.getDir();
					
					for (String string : list) {
						File binDir = fileUtils.resolveFile(basedir, string);
						if (!binDir.exists()) {
							throw new BuildException("binDir \""
									+ binDir.getPath() + "\" does not exist.");
						}
						bin.append(new Path(proj,
    						binDir.getAbsolutePath()));
					}
				}
			}
		}

		public final String getName() {
			return name;
		}

		public final void setName(String name) {
			this.name = name;
		}

		public final File getDir() {
			return dir;
		}

		public final void setDir(File dir) {
			this.dir = dir;
		}

		public void addConfiguredSource(Source src) {
			sources.add(src);
		}

		public void addConfiguredBinary(Binary bin) {
			binaries.add(bin);
		}

		public Path getSources() {
			return src;
		}

		public Path getBinaries() {
			return bin;
		}
		
	}

	public static class Source extends DirSet {
		public Source(){
			super();
		}
	}

	public static class Binary extends DirSet {
		public Binary(){
			super();
		}
	}

	/***************************************************************************
	 * Getters and Setters for attributes
	 **************************************************************************/

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void addConfiguredProject(Project project) {
		this.project = project;
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
	 * @param destDir
	 *            the destDir to set
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
	 * @param clean
	 *            the clean to set
	 */
	public final void setClean(boolean clean) {
		this.clean = clean;
	}

	/**
	 * @return the srcdir
	 */
	public final Path getSrcdir() {
		return srcdir;
	}

	/**
	 * @param srcdir
	 *            the srcdir to set
	 */
	public final void setSrcdir(Path srcdir) {
		this.srcdir.append(srcdir);
	}

	/**
	 * @return the bindir
	 */
	public final Path getBindir() {
		return bindir;
	}

	/**
	 * @param bindir
	 *            the bindir to set
	 */
	public final void setBindir(Path bindir) {
		this.bindir.append(bindir);
	}

	/***************************************************************************
	 * Helper methods taken from Ant's Java task
	 **************************************************************************/

	/**
	 * Executes the given classname with the given arguments in a separate VM.
	 * 
	 * @param command
	 *            String[] of command-line arguments.
	 */
	private int fork(String[] command) throws BuildException {
		Execute exe = new Execute(redirector.createHandler(), createWatchdog());
		setupExecutable(exe, command);

		try {
			int rc = exe.execute();
			redirector.complete();
			if (exe.killedProcess()) {
				throw new BuildException("Timeout: killed the sub-process");
			}
			return rc;
		} catch (IOException e) {
			throw new BuildException(e, getLocation());
		}
	}

	/**
	 * Executes the given classname with the given arguments in a separate VM.
	 * 
	 * @param command
	 *            String[] of command-line arguments.
	 */
	private void spawn(String[] command) throws BuildException {
		Execute exe = new Execute();
		setupExecutable(exe, command);
		try {
			exe.spawn();
		} catch (IOException e) {
			throw new BuildException(e, getLocation());
		}
	}

	/**
	 * Do all configuration for an executable that is common across the
	 * {@link #fork(String[])} and {@link #spawn(String[])} methods.
	 * 
	 * @param exe
	 *            executable.
	 * @param command
	 *            command to execute.
	 */
	private void setupExecutable(Execute exe, String[] command) {
		exe.setAntRun(getProject());
		setupWorkingDir(exe);
		setupEnvironment(exe);
		setupCommandLine(exe, command);
	}

	/**
	 * Set up our environment variables.
	 * 
	 * @param exe
	 *            executable.
	 */
	private void setupEnvironment(Execute exe) {
		String[] environment = env.getVariables();
		if (environment != null) {
			for (int i = 0; i < environment.length; i++) {
				log("Setting environment variable: " + environment[i],
						org.apache.tools.ant.Project.MSG_VERBOSE);
			}
		}
		exe.setNewenvironment(false);
		exe.setEnvironment(environment);
	}

	/**
	 * Set the working dir of the new process.
	 * 
	 * @param exe
	 *            executable.
	 * @throws BuildException
	 *             if the dir doesn't exist.
	 */
	private void setupWorkingDir(Execute exe) {
		if (project.getDir() == null) {
			project.setDir(getProject().getBaseDir());
		} else if (!project.getDir().exists()
				|| !project.getDir().isDirectory()) {
			throw new BuildException(project.getDir().getAbsolutePath()
					+ " is not a valid directory", getLocation());
		}
		exe.setWorkingDirectory(project.getDir());
	}

	/**
	 * Set the command line for the exe. On VMS, hands off to
	 * {@link #setupCommandLineForVMS(Execute, String[])}.
	 * 
	 * @param exe
	 *            executable.
	 * @param command
	 *            command to execute.
	 */
	private void setupCommandLine(Execute exe, String[] command) {
		exe.setCommandline(command);
	}

	/**
	 * Create the Watchdog to kill a runaway process.
	 * 
	 * @return new watchdog.
	 * 
	 * @throws BuildException
	 *             under unknown circumstances.
	 * 
	 * @since Ant 1.5
	 */
	protected ExecuteWatchdog createWatchdog() throws BuildException {
		if (timeout == null) {
			return null;
		}
		return new ExecuteWatchdog(timeout.longValue());
	}

	public CommandlineJava getCommandLine() {
		return cmdl;
	}

	/**
	 * Add a path to the classpath.
	 * 
	 * @return created classpath.
	 */
	public Path createClasspath() {
		return getCommandLine().createClasspath(getProject()).createPath();
	}

}
