/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.Redirector;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.RedirectorElement;
import org.apache.tools.ant.util.FileUtils;

import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;

/**
 * @author ethan
 *
 */
public abstract class ToolConfig implements Runnable {
	protected Redirector redirector;
	protected RedirectorElement redirectorElement;
	private Environment env = new Environment();
	private Long timeout = null;
	private CommandlineJava cmdl = new CommandlineJava();
	
	protected static final FileUtils fileUtils = FileUtils.getFileUtils();
	
	//The Project parent of our SierraAnalysis task
	protected final org.apache.tools.ant.Project antProject;
	
	//The name of the tool that this represents. Should be all lowercase.
	protected String name;
	
	//To get project information
	protected SierraAnalysis analysis;
	
	//Output file for the tool
	protected File output = null;
	
	//Latch used to make sure all tools are done running before the main thread tries to parse their outputs
	protected CountDownLatch latch = null;
	
	protected ToolConfig(String name, org.apache.tools.ant.Project project){
		antProject = project;
		this.name = name;
	}
	
	/**
	 * Must be called by the subclasses via an implementation of {@link #initialize(SierraAnalysis)}
	 * @param name
	 * @param analysis
	 */
	public void initialize(final SierraAnalysis analysis){
		this.analysis = analysis;
		redirector =  new Redirector(analysis);
	}
	
	/**
	 * Validates any attributes of the element
	 * Should be overridden, but called by subclasses
	 */
	protected void validate(){
		if(name == null || analysis == null){
			throw new BuildException(
					"ToolConfig.initialize() must be called before executing this task.");
		}
	}

	/* ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	 * 								START ABSTRACT METHODS
	 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/
	
	/**
	 * Override to parse the output of the specific tools.
	 * @param parser
	 */
	abstract void parseOutput(Parser parser);
	
	/**
	 * Verifies that all of this tool's dependencies are in the classpath before we can throw a NoClassDefFoundError
	 */
	abstract void verifyDependencies();
	
	
	/**
	 * Configures the tool from a Config object
	 */
	abstract void configure(final Config config);
	
	/**
	 * clean up output files
	 */
	abstract void cleanup();

	
	/* $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	 * 								END ABSTRACT METHODS
	 $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$*/
	
	/**
	 * Returns the name of the tool that the config represents
	 */
	public String getToolName(){
		return name;
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
	protected int fork(String[] command) throws BuildException {
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
			throw new BuildException(e, analysis.getLocation());
		}
	}

	/**
	 * Executes the given classname with the given arguments in a separate VM.
	 * 
	 * @param command
	 *            String[] of command-line arguments.
	 */
	protected void spawn(String[] command) throws BuildException {
		Execute exe = new Execute();
		setupExecutable(exe, command);
		try {
			exe.spawn();
		} catch (IOException e) {
			throw new BuildException(e, analysis.getLocation());
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
		exe.setAntRun(analysis.getProject());
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
				antProject.log("Setting environment variable: " + environment[i],
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
		if (analysis.getSierraProject().getDir() == null) {
			analysis.getSierraProject().setDir(antProject.getBaseDir());
		} else if (!analysis.getSierraProject().getDir().exists()
				|| !analysis.getSierraProject().getDir().isDirectory()) {
			throw new BuildException(analysis.getSierraProject().getDir().getAbsolutePath()
					+ " is not a valid directory", analysis.getLocation());
		}
		exe.setWorkingDirectory(analysis.getSierraProject().getDir());
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
		return getCommandLine().createClasspath(analysis.getProject()).createPath();
	}

	/**
	 * Sets the latch, if any, that each tool should touch when it is done.
	 * @param latch
	 */
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}


}
