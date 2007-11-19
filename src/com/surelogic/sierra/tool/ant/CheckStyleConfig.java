package com.surelogic.sierra.tool.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.message.Config;

public class CheckStyleConfig extends ToolConfig {

	private static final String CHECKSTYLE_JAR = "checkstyle-all-4.3.jar";
	private static final String CHECKS_FILE = "sl_sun_checks.xml";
	private static final String CHECKTYLE = "checkstyle-4.3";
	private static final String OUTPUT_FILE = "checkstyle.xml";
	private SLProgressMonitor f_monitor = null;
	private Path f_classpath = null;
	private int f_scale = 1;
	private String f_status = null;

	public CheckStyleConfig(Project project) {
		super("checkstyle", project);
	}

	public CheckStyleConfig(Project antProject, SLProgressMonitor monitor,
			int scale) {
		super("checkstyle", antProject);

		if (monitor != null) {
			this.f_monitor = monitor;
			this.f_scale = scale;
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
		if (f_classpath == null) {
			File libDir = new File(analysis.getTools().getToolsFolder(),
					CHECKTYLE);
			File[] jars = libDir.listFiles(new JarFileFilter());
			File mainJar = new File(analysis.getTools().getToolsFolder(),
					CHECKTYLE + File.separator + CHECKSTYLE_JAR);
			f_classpath = new Path(antProject);
			for (File file : jars) {
				f_classpath
						.append(new Path(antProject, file.getAbsolutePath()));
			}

			f_classpath.append(new Path(antProject, mainJar.getAbsolutePath()));
		}
		return f_classpath;
	}

	@Override
	void parseOutput(Parser parser) {
		if (output != null) {
			for (File file : output) {
				if (file.isFile()) {
					antProject.log("Parsing Checkstyle results file: " + file,
							org.apache.tools.ant.Project.MSG_INFO);

					parser.parseCheckStyle(file.getAbsolutePath());

				}
			}
		}

	}

	@Override
	void verifyDependencies() {
		assert (analysis != null);

		if (!analysis.isJarInClasspath(getClasspath(), CHECKSTYLE_JAR)) {
			throw new BuildException("Reckoner is missing dependency: "
					+ CHECKSTYLE_JAR);

		}

	}

	public void run() {
		if (analysis.keepRunning) {
			analysis.printClasspath(getClasspath());
			// run Checkstyle
			CommandlineJava cmdj = new CommandlineJava();
			File mainJar = new File(analysis.getTools().getToolsFolder(),
					CHECKTYLE + File.separator + CHECKSTYLE_JAR);
			cmdj.setJar(mainJar.getAbsolutePath());

			cmdj.createArgument().setValue("-c");
			File checksFile = new File(analysis.getTools().getToolsFolder(),
					CHECKTYLE + File.separator + CHECKS_FILE);
			cmdj.createArgument().setValue(checksFile.getAbsolutePath());

			cmdj.createArgument().setValue("-f");
			cmdj.createArgument().setValue("xml");

			cmdj.createArgument().setValue("-o");
			output = new File[] { new File(analysis.getTmpFolder(), OUTPUT_FILE) };
			cmdj.createArgument().setPath(
					new Path(antProject, output[0].getAbsolutePath()));

			cmdj.createArgument().setValue("-r");
			String[] paths = analysis.getSrcdir().list();
			for (String string : paths) {
				cmdj.createArgument().setValue(string);
			}

			antProject.log("Executing Checkstyle with the commandline: "
					+ cmdj.toString(), org.apache.tools.ant.Project.MSG_DEBUG);
			try {

				if (f_monitor != null) {
					f_monitor.subTask("Running Checkstyle 4.3");
				}
				int rc = fork(cmdj.getCommandline());
				if ((rc == 1) || (rc == Execute.INVALID)) {
					antProject.log("Checkstyle failed to execute.",
							org.apache.tools.ant.Project.MSG_ERR);
					SLLogger.getLogger("sierra").severe(
							"Checkstyle failed to execute with following command "
									+ cmdj.toString());
					analysis.stop();
					f_status = "Checkstyle execution failed with following command "
							+ cmdj.toString();

				}

			} catch (BuildException e) {
				antProject.log("Failed to start Checkstyle process."
						+ e.getLocalizedMessage(),
						org.apache.tools.ant.Project.MSG_ERR);
			} finally {
				if (latch != null) {
					latch.countDown();
				}

				if (f_monitor != null) {
					f_monitor.worked(f_scale);
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

	@Override
	String getErrorMessage() {
		return f_status;
	}

}
