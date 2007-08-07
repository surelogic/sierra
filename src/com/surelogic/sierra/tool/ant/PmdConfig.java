package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.CommandlineJava;

import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;

/**
 * Represents a configuration attribute for the PMD tool
 * 
 * @author ethan
 * 
 */
public class PmdConfig extends ToolConfig {
	private final static String PMD_JAR = "pmd-3.9.jar";
	private final static String PMD_CLASS = "net.sourceforge.pmd.PMD";

	// The path to the default rules file, relative to the Tools folder
	private static final String RULES_FILE_PATH = "pmd-3.9" + File.separator
			+ "all.xml";

	private String targetJDK = null;
	private File rulesFile = null;

	public PmdConfig(org.apache.tools.ant.Project project) {
		super("pmd", project);
	}

	/**
	 * @see {@link ToolConfig#validate()}
	 */
	@Override
	public void validate() {
		super.validate();

		if (targetJDK != null && !targetJDK.matches("\\d\\.\\d")) {
			throw new BuildException(
					"Invalid version string for pmdconfig's 'javaVersion' attribute. Must be one of the following: 1.3, 1.4, 1.5, 1.6 ");
		}
		if (rulesFile != null && !rulesFile.isFile()) {
			throw new BuildException(
					"rulesfile must be a valid PMD rules XML file.");
			// TODO can we check and make sure it is a valid PMD xml file?
		} else {
			rulesFile = new File(analysis.getSierraTools().getToolsFolder(),
					RULES_FILE_PATH);
		}
	}

	/**
	 * Spins off a PMD process for each element in the source path since PMD
	 * cannot accept multiple directories
	 * 
	 * @see {@link Runnable#run()}
	 */
	public void run() {
		String[] pathDirs = analysis.getSrcdir().list();
		int clientCount = pathDirs.length;
		CountDownLatch pmdLatch = new CountDownLatch(clientCount);
		output = new File[clientCount];

		ExecutorService executor;
		if (analysis.getTools().isMultithreaded()) {
			executor = Executors.newCachedThreadPool();
		} else {
			executor = Executors.newSingleThreadExecutor();
		}

		try {
			for (int i = 0; i < clientCount; i++) {
				executor.execute(new PmdRunner(i, pathDirs[i], pmdLatch));
			}
			pmdLatch.await();
		} catch (InterruptedException e) {
			antProject.log(
					"Error while waiting for all PMD processes to finish.", e,
					org.apache.tools.ant.Project.MSG_ERR);
		} finally {
			if (latch != null) {
				latch.countDown();
			}
		}

	}

	@Override
	public void parseOutput(Parser parser) {
		if (output != null) {
			for (File file : output) {
				if (file.isFile()) {
					antProject.log("Parsing PMD results file: " + file,
							org.apache.tools.ant.Project.MSG_INFO);
					parser.parsePMD(file.getAbsolutePath());
				}
			}
		}
	}

	@Override
	void verifyDependencies() {
		assert (analysis != null);

		if (!analysis.isJarInClasspath(PMD_JAR)) {
			throw new BuildException("PMD is missing dependency: " + PMD_JAR);
		}

	}

	@Override
	void configure(final Config config) {
		setTargetJDK(config.getJavaVersion());
		setRulesFile(config.getPmdRulesFile());
	}

	@Override
	void cleanup() {
		for (File file : output) {
			file.delete();
		}
	}

	/**
	 * @return the rulesFile
	 */
	public final File getRulesFile() {
		return rulesFile;
	}

	/**
	 * @param rulesFile
	 *            the rulesFile to set
	 */
	public final void setRulesFile(File rulesFile) {
		this.rulesFile = rulesFile;
	}

	public void setTargetJDK(String version) {
		this.targetJDK = version;
	}

	public String getTargetJDK() {
		return targetJDK;
	}

	/**
	 * Runs PMD - this is required for running PMD on multiple directories
	 * 
	 * @author ethan
	 * 
	 */
	protected class PmdRunner implements Runnable {
		private final String sourceDir;
		private final CountDownLatch pmdLatch;
		private final int id;

		public PmdRunner(int id, final String sourceDir,
				final CountDownLatch pmdLatch) {
			this.sourceDir = sourceDir;
			this.pmdLatch = pmdLatch;
			this.id = id;
		}

		public void run() {
			// run PMD
			CommandlineJava cmdj = new CommandlineJava();

			// Add the class to run
			cmdj.setClassname(PMD_CLASS);

			// Set the Java command's classpath
			cmdj.createClasspath(antProject).createPath().append(
					analysis.getClasspath());

			// Add optional arguments
			if (targetJDK != null && !"".equals(targetJDK)) {
				cmdj.createArgument().setValue("-targetjdk");
				cmdj.createArgument().setValue(getTargetJDK());
			}

			// Add the output file
			cmdj.createArgument().setValue("-reportfile");

			output[id] = new File(analysis.getTmpFolder(), "pmd-" + id + ".xml");
			try {
				output[id].createNewFile();
			} catch (IOException e1) {
				antProject.log("Error creating PMD output file: "
						+ output[id].getAbsolutePath(),
						org.apache.tools.ant.Project.MSG_ERR);
			}
			cmdj.createArgument().setValue(output[id].getAbsolutePath());

			antProject.log("Classpath: " + cmdj.getClasspath().toString(),
					org.apache.tools.ant.Project.MSG_DEBUG);

			// Add the source directories to scan
			antProject.log("Source path: " + sourceDir,
					org.apache.tools.ant.Project.MSG_DEBUG);

			cmdj.createArgument().setValue(sourceDir);

			// Add the output format
			cmdj.createArgument().setValue("xml");

			// Add the ruleset file
			cmdj.createArgument().setValue(rulesFile.getAbsolutePath());

			antProject.log("Executing PMD with the commandline: "
					+ cmdj.toString(), org.apache.tools.ant.Project.MSG_DEBUG);
			try {

				fork(cmdj.getCommandline());
			} catch (BuildException e) {
				antProject.log("Failed to start PMD process.", e,
						org.apache.tools.ant.Project.MSG_ERR);
			} finally {
				if (pmdLatch != null) {
					pmdLatch.countDown();
				}
			}

		}
	}
}
