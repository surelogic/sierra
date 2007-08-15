/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;

/**
 * @author ethan
 * 
 */
public class FindBugsConfig extends ToolConfig {
	private final static String FINDBUGS_CLASS = "edu.umd.cs.findbugs.FindBugs2";
	private final static String FINDBUGS_JAR = "findbugs.jar";

	// The path to the findbugs location, relative to the tools directory
	private static final String FB_HOME = "FB";

	// The folder to set as findbugs.home
	private File home = null;

	// String passed to Java's -Xmx flag
	private String memory = "1024m";

	private Path classpath = null;
	
	/**
	 * @param project
	 */
	public FindBugsConfig(Project project) {
		super("findbugs", project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.tool.ant.ToolConfig#runTool()
	 */
	public void run() {
		analysis.printClasspath(getClasspath());
		// run FindBugs
		CommandlineJava cmdj = new CommandlineJava();
		cmdj.setClassname(FINDBUGS_CLASS);
		cmdj.setMaxmemory(memory);
		cmdj.createClasspath(antProject).createPath().append(getClasspath());

		cmdj.createArgument().setValue("-xml:withMessages");
		cmdj.createArgument().setValue("-outputFile");
		output = new File[] { new File(analysis.getTmpFolder(), "findbugs.xml") };
		cmdj.createArgument().setPath(
				new Path(antProject, output[0].getAbsolutePath()));
		cmdj.createArgument().setValue("-home");
		cmdj.createArgument().setPath(
				new Path(antProject, getHome().getAbsolutePath()));
		String[] paths = analysis.getBindir().list();
		for (String string : paths) {
			cmdj.createArgument().setValue(string);
		}

		antProject.log("Executing FindBugs with the commandline: "
				+ cmdj.toString(), org.apache.tools.ant.Project.MSG_DEBUG);
		try {
			fork(cmdj.getCommandline());
		} catch (BuildException e) {
			antProject.log("Failed to start FindBugs process.", e,
					org.apache.tools.ant.Project.MSG_ERR);
		} finally {
			if (latch != null) {
				latch.countDown();
			}
		}

	}

	/**
	 * creates the classpath for FindBugs
	 */
	protected Path getClasspath() {
		if (classpath == null) {
			File lib = new File(analysis.getTools().getToolsFolder(), "FB" + File.separator + "lib" + File.separator + FINDBUGS_JAR);
			classpath = new Path(antProject);
			classpath.append(new Path(antProject, lib.getAbsolutePath()));
		}
		return classpath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.tool.ant.ToolConfig#validate()
	 */
	@Override
	public void validate() {
		super.validate();
	}

	@Override
	public void parseOutput(Parser parser) {
		if (output != null) {
			for (File file : output) {
				if (file.isFile()) {
					antProject.log("Parsing FindBugs results file: " + file,
							org.apache.tools.ant.Project.MSG_INFO);
					parser.parseFB(file.getAbsolutePath(), analysis
							.getSourceDirectories());
				}
			}
		}
	}

	@Override
	void verifyDependencies() {
		assert (analysis != null);
		if (!analysis.isJarInClasspath(getClasspath(), FINDBUGS_JAR)) {
			throw new BuildException("FindBugs is missing dependency: "
					+ FINDBUGS_JAR);
		}
	}

	@Override
	void configure(Config config) {
		// nothing to do
	}

	@Override
	void cleanup() {
		for (File file : output) {
			file.delete();
		}
	}

	/**
	 * Return the value for findbugs.home
	 * 
	 * @return the home
	 */
	public final File getHome() {
		if (home == null) {
			home = new File(analysis.getSierraTools().getToolsFolder(), FB_HOME);
		}
		return home;
	}

	/**
	 * Set the value for findbugs.home
	 * 
	 * @param home
	 *            the home to set
	 */
	public final void setHome(File home) {
		this.home = home;
	}

	/**
	 * @return the memory
	 */
	public final String getMemory() {
		return memory;
	}

	/**
	 * @param memory
	 *            the memory to set
	 */
	public final void setMemory(String memory) {
		this.memory = memory;
	}
}
