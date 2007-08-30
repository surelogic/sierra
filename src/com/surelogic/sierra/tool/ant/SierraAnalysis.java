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
 *     <tools  exclude="comma, separated, list, of, tool, names, to, not, run" multithreaded="true" toolsfolder="/path/to/Tools">
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
 *  - toolsfolder - optional, defaults to the path to the jaxb-api.jar/../../ which should be the Tools folder
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.CommandlineJava;
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
	private CommandlineJava cmdl = new CommandlineJava();
	private boolean uploadSuccessful = false;

	private String[] sourceDirectories = null;

	// Used to populate this class and supporting classes, if this class is
	// instantiated from the Sierra client
	private Config config = null;

	// The Date of this run
	private Date runDateTime = null;

	// The classpath created by the Taskdef in the Ant build.xml file
	private Path classpath = null;

	private org.apache.tools.ant.Project antProject = null;

	// Used for when we want to stop the run
	volatile boolean keepRunning = true;

	// Check for classpath dependencies
	private boolean checkCP = true;

	/***************************************************************************
	 * Ant Task Attributes
	 **************************************************************************/

	// Optional attribute, if present, we send the WSDL file to this server
	private String server = null;

	// Optional, but req'd if URL is set. Comma-separated list of qualifiers
	private final List<String> qualifiers = new ArrayList<String>();

	// Optional, if omitted, the system's tmp folder is used
	private File destDir = new File(System.getProperty("java.io.tmpdir"));

	// The file containing the artifacts from the run
	private File runDocument = null;

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

	/**
	 * Constructor
	 */
	public SierraAnalysis() {
		super();
		antProject = getProject();
		log("Project is null? " + (antProject == null));
		srcdir = new Path(antProject);
		bindir = new Path(antProject);
	}

	/**
	 * Constructor used to create this task programmatically from inside the
	 * sierra client used from within eclipse tool run.
	 * 
	 * @param config
	 */
	public SierraAnalysis(Config config) {
		super();
		checkCP = false;

		antProject = new org.apache.tools.ant.Project();
		// antProject.addBuildListener(new CommonsLoggingListener());
		// setProject(antProject);

		this.config = config;
		destDir = config.getDestDirectory();
		runDocument = config.getRunDocument();
		if (config.getClasspath() != null) {
			classpath = new Path(antProject, config.getClasspath());
		}
		clean = config.isCleanTempFiles();
		srcdir = new Path(antProject).createPath();
		srcdir.append(new Path(antProject, config.getSourceDirs()));
		bindir = new Path(antProject).createPath();
		bindir.append(new Path(antProject, config.getBinDirs()));
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
	 * Used by direct access of this class - not via Ant. Halts the running of
	 * this task and any sub-processes it spawned.
	 */
	public void stop() {
		keepRunning = false;
		tools.stop();
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
	 * @param cp
	 * @param jar
	 * @return
	 */
	boolean isJarInClasspath(Path cp, String jar) {
		List<String> list = new ArrayList<String>(1);
		list.add(jar);
		return isJarInClasspath(cp, list);
	}

	/**
	 * Scans the classpath for a specific jar
	 * 
	 * @param cp
	 * @param jar
	 * @return
	 */
	boolean isJarInClasspath(Path cp, List<String> jars) {

		String[] paths = cp.list();
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
	 */
	private void generateRunDocument() {
		if (keepRunning) {
			antProject.log("Generating the Run document...",
					org.apache.tools.ant.Project.MSG_INFO);

			printClasspath(classpath);

			// Fixes a ClassDefNotFoundError on ContextFactory via JAXBContext
			Thread.currentThread().setContextClassLoader(
					this.getClass().getClassLoader());

			// This code computes the source directories from the given base
			// directory

			File root = null;

			if (config == null) {
				config = new Config();
				config.setBaseDirectory(project.getDir());
				config.setProject(project.getName());
				config.setRunDateTime(runDateTime);
				config.setJavaVersion(System.getProperty("java.version"));
				config.setJavaVendor(System.getProperty("java.vendor"));
				config.setQualifiers(qualifiers);

				root = project.getDir();
			} else {
				// FIXME: Hack for providing the java version as PMD rejects the
				// String returned from System.getProperty
				config.setJavaVersion(System.getProperty("java.version"));
				root = config.getBaseDirectory();
			}

			JavaFilter filter = new JavaFilter();
			filterdirs(root, filter);

			Iterator<File> dirIterator = filter.dirs.iterator();
			List<String> sourceDirectory = new ArrayList<String>();
			while (dirIterator.hasNext()) {
				File holder = dirIterator.next();
				sourceDirectory.add(holder.getPath());
			}

			sourceDirectories = sourceDirectory
					.toArray(new String[sourceDirectory.size()]);

			if (runDocument == null || "".equals(runDocument)) {
				runDocument = new File(tmpFolder, project.getName() + ".xml"
						+ PARSED_FILE_SUFFIX);
			} else if (runDocument.isDirectory()) {
				runDocument = new File(runDocument, project.getName() + ".xml"
						+ PARSED_FILE_SUFFIX);
			} else if (!runDocument.getName().endsWith(
					".xml" + PARSED_FILE_SUFFIX)) {
				runDocument = new File(runDocument.getParentFile(), runDocument
						.getName()
						+ ".xml" + PARSED_FILE_SUFFIX);
			}

			antProject.log("Generating the run document: " + runDocument,
					org.apache.tools.ant.Project.MSG_INFO);
			MessageArtifactFileGenerator generator = new MessageArtifactFileGenerator(
					runDocument, config);
			Parser parser = new Parser(generator);
			tools.parseOutput(parser);
			generator.finished();

		}
	}

	void printClasspath(Path path) {

		if (path != null) {
			String[] classpathList = path.list();

			antProject.log("---------- CLASSPATH ----------",
					org.apache.tools.ant.Project.MSG_VERBOSE);
			for (String string : classpathList) {
				antProject.log(string, org.apache.tools.ant.Project.MSG_VERBOSE);
			}
		}
	}

	void printClasspathToStdOut(Path path) {
		if (path != null) {
			String[] classpathList = path.list();

			System.out.println("---------- CLASSPATH ----------");
			for (String string : classpathList) {
				System.out.println(string);
			}
		}
	}

	/**
	 * Optional action. Uploads the generated WSDL file to the desired server.
	 */
	private void uploadRunDocument() {
		if (keepRunning) {
			antProject.log("Uploading the Run document to " + server + "...",
					org.apache.tools.ant.Project.MSG_INFO);
			MessageWarehouse warehouse = MessageWarehouse.getInstance();
			Run run = warehouse.fetchRun(runDocument.getAbsolutePath());
			TigerService ts = new TigerServiceClient(server)
					.getTigerServicePort();

			// Verify the qualifiers
			List<String> list = ts.getQualifiers().getQualifier();
			if (!list.containsAll(qualifiers)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid qualifiers. Valid qualifiers are:\n");
				for (String string : list) {
					sb.append(string);
					sb.append("\n");
				}
				throw new BuildException(sb.toString());
			}
			// FIXME utilize the return value once Bug 867 is resolved
			if (ts.publishRun(run).equalsIgnoreCase("failure")) {
				antProject.log("Failed to upload run document, "
						+ runDocument.getAbsolutePath() + " to the server: "
						+ server, org.apache.tools.ant.Project.MSG_ERR);
				uploadSuccessful = false;
			}
			uploadSuccessful = true;
		}
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
		if (keepRunning) {
			antProject.log("Cleaning up...", org.apache.tools.ant.Project.MSG_INFO);
			tools.cleanup();
			// If we uploaded successfully, delete our run document and temp
			// directory
			if (uploadSuccessful) {
				runDocument.delete();
				tmpFolder.delete();
			}
		}
	}

	/**
	 * Verifies that all of the appropriate jars exist on the classpath
	 */
	private void verifyDependencies() {
		if (keepRunning && checkCP) {
			tools.verifyToolDependencies();

			String missing = findMissingJarFromClasspath(DEPENDENCIES);
			if (missing != null) {
				throw new BuildException("Missing dependency: " + missing);
			}
		}
	}

	/**
	 * Ensures all properties/parameters are set and valid If any parameters are
	 * invalid, this method will throw a BuildException
	 */
	private void validateParameters() {
		if (keepRunning) {
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

			if (srcdir.size() == 0) {
				// We can do this w/o checking b/c the project was validated
				// previously
				srcdir.append(new Path(antProject, project.getDir()
						.getAbsolutePath()));
			} else {
				validatePath(srcdir); // throws BuildException if it has an
				// non-valid path element
				srcdir.append(project.getSources());
			}

			if (bindir.size() == 0) {
				antProject.log("No value set for 'bindir' or 'binaries'. Values for 'srcdir' or 'sources' will be used.");
				bindir.append(srcdir);
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
				classpath.append(new Path(antProject, ((AntClassLoader) loader)
						.getClasspath()));
			} else {
				classpath.append(new Path(antProject, System
						.getProperty("java.class.path")));
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

	/**
	 * Get the source directories for FindBugs parsing
	 * 
	 * @return
	 */
	public String[] getSourceDirectories() {
		return sourceDirectories;
	}

}
