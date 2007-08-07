/**
 * 
 * <sierra-analysis  
 *     destDir="/tool/results/output/dir"
 *     srcdir="/source/directory"  
 *     bindir="/path/to/.class/files"
 *     server="sierra.server.address:port"  
 *     qualifiers="comma, separated, list, of, qualifiers"
 *     runDocument="/path/to/run/document.xml.parsed"
 *     clean="true">
 * 
 *     <project  name="project"  dir="/directory/for/project">
 *         <source  dir="/path/to/directory/containing/directories/containing/.java/files">
 *             <include  name="package/p1/**"/>
 *             <exclude  name="package/p1/** /test* />
 *         </source>
 * 
 *         <binary  dir="/path/to/directory/containing/directories/containing/.class/files">
 *             <include  name="package/p1/**"/>  
 *             <exclude  name="package/p1/** /test* />
 *         </binary>       
 *     </project>
 * 
 *     <tools  exclude="comma, separated, list, of, tool, names, to, not, run" multithreaded="true">
 *         <pmdconfig javaVersion=�1.5� rulefile="/path/to/rule/file.xml"/>
 *         <findbugsconfig memory="1024m"/>
 *     </tools>
 * </sierra-analysis>
 * 
 * The following attributes are optional:
 * destDir - defaults to /tmp, the directory to store tools' results
 * Srcdir - required unless nested <source> elements are defined
 * BinDir - required unless nested <binary> elements are defined
 * Server - if set, will attempt to send the WSDL file to this address. The port is optional
 * qualifiers - comma-separated list of qualifiers, must be valid as per the {@link Qualifier} class.
 * Clean - if set to true, will clean the tool result files and the WSDL (if it
 * runDocument - the full path to the desired run document
 * was sent to the server successfully)
 * 
 * Source - actually a DirSet
 * 	- multiple can be defined
 *     - include - same setup as in most ant tasks
 *     - exclude - ditto
 * 
 * Binary - actually a DirSet
 * 	- multiple can be defined
 *     - if this is not defined, it will use the source settings
 * 
 * Tools - if not defined, will run all of the tools
 * 	- exclude - optional
 *  - multithreaded - optional, defaults to false
 * 	- pmdconfig - optional
 * 		- javaVersion - optional, PMD defaults to 1.5
 * 		- rulefile - optional, defaults to ${tools directory}/pmd-3.9/all.xml
 *  - findbugsconfig - optional
 *      - memory - optional, the string to send to Java's -Xmx commandline option, defaults to '1024m'
 *      - home - optional, the value to set for the findbugs.home variable
 * 
 * The tools' jar files will be included in the <taskdef> that loads this
 * task's jar.
 *
 *
 * <b>NOTE</b>
 * To add support for more tools, see the comments at the top of {@link Tools}.
 *
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.RedirectorElement;

import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;
import com.surelogic.sierra.tool.message.MessageArtifactFileGenerator;
import com.surelogic.sierra.tool.message.MessageWarehouse;
import com.surelogic.sierra.tool.message.Run;
import com.surelogic.sierra.tool.message.TigerService;
import com.surelogic.sierra.tool.message.TigerServiceClient;

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
	private boolean uploadSuccessful = false;

	private String[] sourceDirectories = null;

	// Used to populate this class and supporting classes, if this class is
	// instantiated from the Sierra client
	private Config config = null;

	// The file containing the artifacts from the run
	private File runDocument = null;

	// The Date of this run
	private Date runDateTime = null;

	// The classpath created by the Taskdef in the Ant build.xml file
	private Path classpath = null;

	private org.apache.tools.ant.Project antProject = null;

	/***************************************************************************
	 * Ant Task Attributes
	 **************************************************************************/

	// Optional attribute, if present, we send the WSDL file to this server
	private String server = null;

	// Optional, but req'd if URL is set. Comma-separated list of qualifiers
	private final List<String> qualifiers = new ArrayList<String>();

	// Optional, if omitted, the system's tmp folder is used
	private File destDir = new File(System.getProperty("java.io.tmpdir"));

	// Optional file attribute.
	// Where to store the temp files
	private File tmpFolder = null;

	// Required
	private Project project = null;

	// Optional, defaults to false
	private boolean clean = false;

	// Optional, if omitted, all tools are run and PMD's java version is set to
	// 1.5
	private Tools tools = null;

	// Optional
	private Path srcdir = null;

	// Optional
	private Path bindir = null;

	/* *********************** CONSTANTS ****************************** */
	private static final String PARSED_FILE_SUFFIX = ".parsed";
	private static final List<String> DEPENDENCIES = new ArrayList<String>(4);

	static {
		DEPENDENCIES.add("jaxb-api.jar");
		DEPENDENCIES.add("jaxb-impl.jar");
		DEPENDENCIES.add("jsr173_api.jar");
		DEPENDENCIES.add("backport-util-concurrent.jar");
	}

	// Common initializer code
	{
		antProject = getProject();
		srcdir = new Path(antProject);
		bindir = new Path(antProject);
	}

	/**
	 * Constructor
	 */
	public SierraAnalysis() {
		super();
	}

	/**
	 * Constructor used to create this task programmatically from inside the
	 * sierra client NOT CURRENTLY USED.
	 * 
	 * @param config
	 */
	public SierraAnalysis(Config config) {
		super();
		this.config = config;
		destDir = config.getDestDirectory();
		runDocument = config.getRunDocument();
		classpath = new Path(antProject, config.getClasspath());
		clean = config.isCleanTempFiles();
		srcdir = new Path(antProject, config.getSourceDirs());
		bindir = new Path(antProject, config.getBinDirs());
		tools = new Tools(antProject, config);
		project = new Project(antProject, config);
	}

	/**
	 * @see Task
	 */
	public void execute() {
		if (runDateTime == null) {
			runDateTime = Calendar.getInstance().getTime();
		}

		validateParameters();
		verifyDependencies();
		tools.runTools();
		generateRunDocument();
		if (server != null && !"".equals(server)) {
			uploadRunDocument();
		}

		if (clean) {
			cleanup();
		}
	}

	/**
	 * Converts an array of strings into a string of comma-separated values
	 * 
	 * @param paths
	 * @return
	 */
	String arrayToCSV(String[] paths) {
		StringBuilder csv = new StringBuilder();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				csv.append(paths[i]);
				// append a comma if we are not on the last element
				if (i < paths.length - 1) {
					csv.append(", ");
				}
			}
		}
		return csv.toString();
	}

	/**
	 * Helper method for {@link }
	 * 
	 * @param jar
	 * @return
	 */
	boolean isJarInClasspath(String jar) {
		List<String> list = new ArrayList<String>(1);
		list.add(jar);
		return isJarInClasspath(list);
	}

	/**
	 * Scans the classpath for a specific jar
	 * 
	 * @param jar
	 * @return
	 */
	boolean isJarInClasspath(List<String> jars) {

		String[] paths = getClasspath().list();
		boolean found = false;
		outer: for (String path : paths) {
			for (String jar : jars) {
				if (path.endsWith(jar)) {
					found = true;
					break outer;
				}
			}
		}
		return found;
	}

	/**
	 * 
	 * @param jars
	 * @return The name of the 1st missing jar or null if none are missing
	 */
	String findMissingJarFromClasspath(List<String> jars) {
		String[] paths = getClasspath().list();
		boolean found = false;
		String missing = null;
		for (String jar : jars) {
			found = false;
			for (String path : paths) {
				if (path.endsWith(jar)) {
					found = true;
					break;
				}
			}
			if (!found) {
				missing = jar;
				break;
			}
		}
		return missing;
	}

	/**
	 * Generates a WSDL file from the updated database
	 * 
	 * @see {@link Launcher#parseFiles()}
	 */
	private void generateRunDocument() {
		log("Generating the Run document...",
				org.apache.tools.ant.Project.MSG_INFO);

		printClasspath();

		// Fixes a ClassDefNotFoundError on ContextFactory via JAXBContext
		Thread.currentThread().setContextClassLoader(
				this.getClass().getClassLoader());

		if (config == null) {
			config = new Config();
			config.setBaseDirectory(project.getDir().getAbsolutePath());
			config.setProject(project.getName());
			config.setRunDateTime(runDateTime);
			config.setJavaVersion(System.getProperty("java.version"));
			config.setJavaVendor(System.getProperty("java.vendor"));
			config.setQualifiers(qualifiers);

			// This code computes the source directories from the given base
			// directory
			File root = new File(config.getBaseDirectory());
			JavaFilter filter = new JavaFilter();
			filterdirs(root, filter);

			Iterator<File> dirIterator = filter.dirs.iterator();
			Vector<String> sourceDirectory = new Vector<String>();
			while (dirIterator.hasNext()) {
				File holder = dirIterator.next();
				sourceDirectory.add(holder.getPath());
			}

			sourceDirectories = sourceDirectory
					.toArray(new String[sourceDirectory.size()]);
		}

		if (runDocument == null || "".equals(runDocument)) {
			runDocument = new File(tmpFolder, project.getName() + ".xml"
					+ PARSED_FILE_SUFFIX);
		} else if (runDocument.isDirectory()) {
			runDocument = new File(runDocument, project.getName() + ".xml"
					+ PARSED_FILE_SUFFIX);
		} else if (!runDocument.getName().endsWith(".xml" + PARSED_FILE_SUFFIX)) {
			runDocument = new File(runDocument.getParentFile(), runDocument
					.getName()
					+ ".xml" + PARSED_FILE_SUFFIX);
		}

		log("Generating the run document: " + runDocument,
				org.apache.tools.ant.Project.MSG_INFO);
		MessageArtifactFileGenerator generator = new MessageArtifactFileGenerator(
				runDocument.getAbsolutePath(), config);
		Parser parser = new Parser(generator);

		tools.parseOutput(parser);
		generator.write();

	}

	private void printClasspath() {
		String[] classpathList = classpath.list();

		log("---------- CLASSPATH ----------",
				org.apache.tools.ant.Project.MSG_DEBUG);
		for (String path : classpathList) {
			log(path, org.apache.tools.ant.Project.MSG_DEBUG);
		}
	}

	/**
	 * Optional action. Uploads the generated WSDL file to the desired server.
	 */
	private void uploadRunDocument() {
		log("Uploading the Run document to " + server + "...",
				org.apache.tools.ant.Project.MSG_INFO);
		MessageWarehouse warehouse = MessageWarehouse.getInstance();
		Run run = warehouse.fetchRun(runDocument.getAbsolutePath());
		TigerService ts = new TigerServiceClient(server).getTigerServicePort();
		
		//Verify the qualifiers
		// List<String> list = ts.getQualifiers().getQualifier();
		// if (!list.containsAll(qualifiers)) {
		// StringBuilder sb = new StringBuilder();
		// sb.append("Invalid qualifiers. Valid qualifiers are:\n");
		// for (String string : list) {
		// sb.append(string);
		// sb.append("\n");
		// }
		// throw new BuildException(sb.toString());
		//		}
		// FIXME utilize the return value once Bug 867 is resolved
		if(ts.publishRun(run).equalsIgnoreCase("failure")){
			log("Failed to upload run document, " + runDocument.getAbsolutePath() + " to the server: " + server, org.apache.tools.ant.Project.MSG_ERR);
			uploadSuccessful = false;
		}
		uploadSuccessful = true;
	}

	/**
	 * Returns true if all folders in the given path are valid
	 * 
	 * @param path
	 * @throws BuildException
	 *             if a path element is not a valid directory
	 */
	private void validatePath(Path path) throws BuildException {
		String[] list = path.list();

		for (String string : list) {
			File dir = new File(string);
			if (!dir.exists()) {
				throw new BuildException("Path element \"" + dir.getPath()
						+ "\" does not exist.");
			}
		}
	}

	/**
	 * Cleans up after the task
	 */
	private void cleanup() {
		log("Cleaning up...", org.apache.tools.ant.Project.MSG_INFO);
		tools.cleanup();
		// If we uploaded successfully, delete our run document and temp directory
		if (uploadSuccessful) {
			runDocument.delete();
			tmpFolder.delete();
		}
	}

	/**
	 * Verifies that all of the appropriate jars exist on the classpath
	 */
	private void verifyDependencies() {
		tools.verifyToolDependencies();

		String missing = findMissingJarFromClasspath(DEPENDENCIES);
		if (missing != null) {
			throw new BuildException("Missing dependency: " + missing);
		}
	}

	/**
	 * Ensures all properties/parameters are set and valid If any parameters are
	 * invalid, this method will throw a BuildException
	 */
	private void validateParameters() {
		if (destDir != null && !destDir.isDirectory()) {
			throw new BuildException("'destdir' must be a valid directory.");
		} else {
			tmpFolder = new File(destDir, "Sierra-analysis-"
					+ project.getName() + "-" + +System.currentTimeMillis());
			if (!tmpFolder.mkdir()) {
				throw new BuildException(
						"Could not create temporary output directory");
			}
		}

		if (server != null) {
			if ("".equals(server)) {
				throw new BuildException("server must not be blank");
			} else {
				if (!server.matches("(\\w)+(\\.(\\w)+)*(:\\d+)?")) {
					throw new BuildException(
							"The server address must be in the form: server.address.com[:port]");
				}
				if (qualifiers.isEmpty()) {
					throw new BuildException(
							"serverQualifiers must contain one or more, comma-separated qualifiers.");
				}
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
			validatePath(srcdir); // throws BuildException if it has an
			// non-valid path element
			srcdir.append(project.getSources());
		}

		if (bindir == null) {
			log("No value set for 'bindir' or 'binaries'. Values for 'srcdir' or 'sources' will be used.");
		} else {
			validatePath(bindir); // throws BuildException if it has an
			// non-valid path element
			bindir.append(project.getBinaries());
		}

		if (tools == null) {
			tools = new Tools(getProject());
		}
		tools.initialize(this);
		tools.validate();

	}

	/***************************************************************************
	 * Getters and Setters for attributes
	 **************************************************************************/

	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}

	public void addConfiguredProject(Project project) {
		this.project = project;
	}

	public void addConfiguredTools(Tools tools) {
		this.tools = tools;
	}

	Tools getTools() {
		return tools;
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
		if (bindir.list().length == 0) {
			return srcdir;
		}
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

	/**
	 * @return the serverQualifier
	 */
	public final List<String> getQualifiers() {
		return qualifiers;
	}

	/**
	 * @param serverQualifier
	 *            the serverQualifier to set
	 */
	public final void setQualifiers(String qualifiers) {
		String[] q = qualifiers.split(",");
		for (String qualifier : q) {
			this.qualifiers.add(qualifier.trim());
		}
	}

	/**
	 * @return the runDocument file
	 */
	public final File getRunDocument() {
		return runDocument;
	}

	/**
	 * @param runDocument
	 *            the runDocument to set
	 */
	public final void setRunDocument(File runDocument) {
		this.runDocument = runDocument;
	}

	/**
	 * Returns the file representing the dynamically created, unique folder on
	 * the disk where this run's output is placed. It is inside the destDir
	 * folder.
	 * 
	 * @return the tmpFolder
	 */
	final File getTmpFolder() {
		return tmpFolder;
	}

	/**
	 * Returns the classpath of this Task, retrieved from the AntClassLoader
	 * that loaded our SierraAnalysis
	 * 
	 * @return the classpath
	 */
	final Path getClasspath() {
		if (classpath == null) {
			classpath = new Path(getProject());

			ClassLoader loader = this.getClass().getClassLoader();
			if (loader != null && loader instanceof AntClassLoader) {
				classpath.append(new Path(getProject(),
						((AntClassLoader) loader).getClasspath()));
			}
		}
		return classpath;
	}

	/**
	 * @return the project
	 */
	final Project getSierraProject() {
		return project;
	}

	/**
	 * Returns the Tools object
	 * 
	 * @return the tools object
	 */
	final Tools getSierraTools() {
		return tools;
	}

	/**
	 * Code for getting the source directories
	 * 
	 * @author nathan
	 * 
	 */
	private static class JavaFilter implements FilenameFilter {

		HashSet<File> dirs = new HashSet<File>();

		public boolean accept(File dir, String name) {
			if (name.endsWith(".java")) {
				dirs.add(dir);
			}
			return false;
		}
	}

	private static void filterdirs(File root, FilenameFilter filter) {
		root.list(filter);
		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				filterdirs(f, filter);
			}
		}
	}

	public String[] getSourceDirectories() {
		return sourceDirectories;
	}

}
