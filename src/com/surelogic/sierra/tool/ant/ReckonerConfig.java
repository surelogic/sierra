package com.surelogic.sierra.tool.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;

public class ReckonerConfig extends ToolConfig {

	// private static final String RECKONER_CLASS =
	// "com.surelogic.sierra.metrics.analysis.main";

	private static final String RECKONER_JAR = "reckoner.jar";

	private SLProgressMonitor monitor = null;

	// /** base directory if different than the default */
	// private String baseDir = null;

	private Path classpath = null;

	public ReckonerConfig(Project project) {
		super("reckoner", project);
	}

	public ReckonerConfig(Project antProject, SLProgressMonitor monitor) {
		super("reckoner", antProject);

		if (monitor != null) {
			this.monitor = monitor;
		}
	}

	@Override
	void cleanup() {
		for (File file : output) {
			file.delete();
		}

	}

	@Override
	protected Path getClasspath() {
		if (classpath == null) {
			File libDir = new File(analysis.getTools().getToolsFolder(),
					"reckoner" + File.separator + "lib");
			File[] jars = libDir.listFiles(new JarFileFilter());
			File mainJar = new File(analysis.getTools().getToolsFolder(),
					"reckoner" + File.separator + RECKONER_JAR);
			classpath = new Path(antProject);
			for (File file : jars) {
				classpath.append(new Path(antProject, file.getAbsolutePath()));
			}

			classpath.append(new Path(antProject, mainJar.getAbsolutePath()));
		}
		return classpath;
	}

	@Override
	void parseOutput(Parser parser) {
		if (output != null) {
			for (File file : output) {
				if (file.isFile()) {
					antProject.log("Appending Reckoner results file: " + file,
							org.apache.tools.ant.Project.MSG_INFO);
					parser.parseReckoner(file.getAbsoluteFile());

				}
			}
		}

	}

	@Override
	void verifyDependencies() {
		assert (analysis != null);

		if (!analysis.isJarInClasspath(getClasspath(), RECKONER_JAR)) {
			throw new BuildException("Reckoner is missing dependency: "
					+ RECKONER_JAR);

		}

	}

	public void run() {
		if (analysis.keepRunning) {
			analysis.printClasspath(getClasspath());
			// run FindBugs
			CommandlineJava cmdj = new CommandlineJava();
			File mainJar = new File(analysis.getTools().getToolsFolder(),
					"reckoner" + File.separator + RECKONER_JAR);
			cmdj.setJar(mainJar.getAbsolutePath());
			// cmdj.createClasspath(antProject).createPath()
			// .append(getClasspath());

			cmdj.createArgument().setValue("-outputFile");
			output = new File[] { new File(analysis.getTmpFolder(),
					"reckoner.xml") };
			cmdj.createArgument().setPath(
					new Path(antProject, output[0].getAbsolutePath()));
			cmdj.createArgument().setValue("-target");

			// FIXME: Reckoner currently can't handle comma separated targets,
			// source dir should be the root of the project
			String[] paths = analysis.getSrcdir().list();
			for (String string : paths) {
				cmdj.createArgument().setValue(string);
			}

			antProject.log("Executing Reckoner with the commandline: "
					+ cmdj.toString(), org.apache.tools.ant.Project.MSG_DEBUG);
			try {

				if (monitor != null) {
					monitor.subTask("Running Reckoner");
				}
				fork(cmdj.getCommandline());

			} catch (BuildException e) {
				antProject.log("Failed to start Reckoner process."
						+ e.getLocalizedMessage(),
						org.apache.tools.ant.Project.MSG_ERR);
			} finally {
				if (latch != null) {
					latch.countDown();
				}

				if (monitor != null) {
					monitor.worked(1);
				}

			}
		} else {
			if (latch != null) {
				latch.countDown();
			}
		}

	}

	@Override
	void configure(Config config) {
		// Nothing to do

	}

}
