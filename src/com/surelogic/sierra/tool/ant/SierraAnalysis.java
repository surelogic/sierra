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

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteJava;
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
public class SierraAnalysis extends Task {
	protected Redirector redirector = new Redirector(this);
	protected RedirectorElement redirectorElement;
	private Environment env = new Environment();
	private Long timeout = null;
	private CommandlineJava cmdl = new CommandlineJava();
	private static final FileUtils fileUtils = FileUtils.getFileUtils();
	private File pmdOutput = null;
	private File fbOutput = null;

	private org.apache.tools.ant.Project proj = getProject();

	// Optional attribute, if present, we send the WSDL file to this server
	private String serverURL = null;

	// Optional, but req'd if URL is set
	// TODO - getters/setters and validation
	private String serverQualifier = null;

	// Optional, if omitted, the system's tmp folder is used
	private File destDir = new File(System.getProperty("java.io.tmpdir"));

	// Req'd name for the WSDL (run document)
	// TODO
	private String runDocumentName = null;

	// Optional file attribute.
	// Where to store the temp files
	// TODO
	private File tmpFolder = null;

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
	private final static String FINDBUGS = "findbugs";
	private final static String PMD = "pmd";
	public final static String[] toolList = new String[] { FINDBUGS, PMD };
	private final static String FINDBUGS_CLASS = "edu.umd.cs.findbugs.FindBugs2";
	private final static String PMD_CLASS = "net.sourceforge.pmd.PMD";
	private static final String MAX_MEMORY = "1024";

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
		log("Results will be saved to: " + tmpFolder,
				org.apache.tools.ant.Project.MSG_DEBUG);

		Path classpath = new Path(getProject());
		
		ClassLoader loader = this.getClass().getClassLoader();
		if (loader != null && loader instanceof AntClassLoader) {
			classpath.append(new Path(getProject(), ((AntClassLoader) loader)
					.getClasspath()));
		}

		if (tools == null || Arrays.binarySearch(tools.getExclude(), PMD) < 0) {
			// run PMD
			CommandlineJava cmdj = new CommandlineJava();

			// Add the class to run
			cmdj.setClassname(PMD_CLASS);

			// Set the Java command's classpath
			cmdj.createClasspath(getProject()).createPath().append(classpath);

			log("Classpath: " + cmdj.getClasspath().toString(),
					org.apache.tools.ant.Project.MSG_DEBUG);

			// Add the source directories to scan
			String[] paths = srcdir.list();
			String csv = arrayToCSV(paths);
			log("Source path: " + csv, org.apache.tools.ant.Project.MSG_DEBUG);
			
			cmdj.createArgument().setValue(csv);

			// Add the output format
			cmdj.createArgument().setValue("xml");

			// Add the ruleset file
			cmdj
					.createArgument()
					.setValue(
							"/Users/ethan/sierra-workspace/sierra-tool/Tools/pmd-3.9/all.xml");

			// Add optional arguments
			if (tools != null && tools.getPmdConfig() != null) {
				cmdj.createArgument().setValue("-targetjdk");
				cmdj.createArgument().setValue(
						tools.getPmdConfig().getJavaVersion());
			}

			log("Executing PMD with the commandline: " + cmdj.toString(),
					org.apache.tools.ant.Project.MSG_DEBUG);
			try {
				pmdOutput = new File(tmpFolder, "pmd.xml");
				redirector.setOutput(pmdOutput);
				
				fork(cmdj.getCommandline());
			} catch (BuildException e) {
				log("Failed to start PMD process.", e,
						org.apache.tools.ant.Project.MSG_ERR);
			}
		}

		if (tools == null
				|| Arrays.binarySearch(tools.getExclude(), FINDBUGS) < 0) {
			// run FindBugs
			CommandlineJava cmdj = new CommandlineJava();
			cmdj.setClassname(FINDBUGS_CLASS);
			cmdj.setMaxmemory(MAX_MEMORY);
			cmdj.createClasspath(getProject()).createPath().append(classpath);

//			cmdj.createArgument().setValue("-textui");
			cmdj.createArgument().setValue("-xml");
			cmdj.createArgument().setValue("-outputFile");
			cmdj.createArgument().setValue(tmpFolder.getAbsolutePath() + File.separator + "findbugs.xml");
			cmdj.createArgument().setValue("-home");
			cmdj.createArgument().setPath(new Path(proj, "/Users/ethan/sierra-workspace/sierra-tool/Tools/FB"));
			String[] paths = bindir.list();
			for (String string : paths) {
    			cmdj.createArgument().setValue(string);
			}

			log("Executing FindBugs with the commandline: " + cmdj.toString(),
					org.apache.tools.ant.Project.MSG_DEBUG);
			try {
				redirector.setOutput((File)null);
				fork(cmdj.getCommandline());
			} catch (BuildException e) {
				log("Failed to start FindBugs process.", e,
						org.apache.tools.ant.Project.MSG_ERR);
			}
		}
	}

	private String arrayToCSV(String[] paths) {
		StringBuilder csv = new StringBuilder();
		for (int i = 0; i < paths.length - 1; i++) {
			csv.append(paths[i]);
			csv.append(", ");
		}
		// add the last item at the end w/o a trailing comma
		csv.append(paths[paths.length - 1]);
		return csv.toString();
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
		} else {
			tmpFolder = new File(destDir, "Sierra-analysis-"
					+ System.currentTimeMillis());
			if (!tmpFolder.mkdir()) {
				throw new BuildException(
						"Could not create temporary output directory");
			}
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
			srcdir.append(project.getSources());
		}

		if (bindir == null) {
			log("No value set for 'bindir' or 'binaries'. Values for 'srcdir' or 'sources' will be used.");
		} else {
			// TODO run through the path and verify all directories are valid
			bindir.append(project.getBinaries());
		}

		if (tools != null) {
			tools.validate();
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
