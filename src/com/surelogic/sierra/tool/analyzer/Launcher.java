package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.ant.Formatter;
import net.sourceforge.pmd.ant.PMDTask;
import net.sourceforge.pmd.ant.RuleSetWrapper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;

import com.surelogic.sierra.SierraTool;
import com.surelogic.sierra.SierraLogger;
import com.surelogic.sierra.message.MessageArtifactGenerator;
import com.surelogic.sierra.tool.config.BaseConfig;
import com.surelogic.sierra.tool.config.ToolConfig;

import edu.umd.cs.findbugs.ExitCodes;

/**
 * The launcher for the tools using ANT api
 * 
 * @author Tanmay.Sinha
 * 
 */
public abstract class Launcher {

	private Java findbugsTask;

	private String findBugsHome;

	private BaseConfig baseConfig;

	private String tmpDir;

	private String resultsFolder;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	private static final String TIGER_RESULTS = ".TigerResult";

	private static final Long TIMEOUT = 600000L;

	private static final String FINDBUGS_JAR = "findbugs.jar";

	public Launcher(String Name, BaseConfig baseConfig) {
		log.info("\nSTARTING LAUNCHER...\n");

		tmpDir = System.getProperty("java.io.tmpdir");
		resultsFolder = tmpDir + File.separator + TIGER_RESULTS;
		File resultRoot = new File(resultsFolder);
		this.baseConfig = baseConfig;

		if ((!resultRoot.exists()) || (resultRoot.exists())
				&& (!resultRoot.isDirectory())) {

			resultRoot.mkdir();
		}
	}

	private static Project createProject() {
		Project project = new Project();
		project.init();
		return project;
	}

	/**
	 * Add an argument to the JVM used to execute FindBugs.
	 * 
	 * @param arg
	 *            the argument
	 */
	private void addArg(String arg) {
		findbugsTask.createArg().setValue(arg);
	}

	/**
	 * Launch FindBugs
	 * 
	 * @param findBugsToolConfig
	 */
	public void launchFB() {
		log.info("Launching FindBugs tool.");

		String baseDirectory = baseConfig.getBaseDirectory();
		String resultsPath = resultsFolder + File.separator
				+ baseConfig.getProjectName();
		String pluginDirectory = baseConfig.getToolsDirectory();

		File resultDirectory = new File(resultsPath);

		if (!resultDirectory.exists()) {
			resultDirectory.mkdir();
		}

		log
				.info("Checked directory for results from Findbugs. Now launching task.");

		try {
			Project p = createProject();
			findbugsTask = (Java) p.createTask("java");

			findbugsTask.setTaskName("FindBugsAnt");
			findbugsTask.setFork(true);
			findbugsTask.createJvmarg().setLine("-Xmx1024M");
			findbugsTask.setTimeout(TIMEOUT);

			// TODO: [Bug 777] Fix this along with FindBugs. The execution of
			// findbugs must be changed from the current way.

			findBugsHome = pluginDirectory + "Tools" + File.separator + "FB";

			log.info("______" + findBugsHome);

			File findBugsJar = new File(findBugsHome + File.separator + "lib"
					+ File.separator + FINDBUGS_JAR);

			findbugsTask.setJar(findBugsJar);

			addArg("-home");
			addArg(findBugsHome);
			addArg("-outputFile");
			addArg(resultDirectory + File.separator + "FB--"
					+ baseConfig.getProjectName() + ".xml");
			addArg("-xml:withMessages");
			addArg(baseDirectory);

			// log.info("Running FindBugs...");

			// log.info(findbugsTask.getCommandLine().describeCommand());

			int rc = findbugsTask.executeJava();

			if ((rc & ExitCodes.ERROR_FLAG) != 0) {
				throw new BuildException("Execution of findbugs failed.");
			}
			if ((rc & ExitCodes.MISSING_CLASS_FLAG) != 0) {
				log.info("Classes needed for analysis were missing");
			}

			// log.info("Output saved to " + resultDirectory + File.separator
			// + "FB--" + findBugsToolConfig.getProjectName() + ".xml");

			log.info("FindBugs tool has finished, now parsing file.");
			MessageArtifactGenerator generator = new MessageArtifactGenerator();
			Parser parser = new Parser(generator);
			parser.parseFB(resultDirectory + File.separator + "FB--"
					+ baseConfig.getProjectName() + ".xml", baseConfig
					.getSourceDirectories());
			log.info("Findbugs file parsed.");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to run findbugs" + e);
		}

		log.info("Completed Findbugs execution.");
	}

	/**
	 * Launch PMD
	 * 
	 * @param pmdToolConfig
	 */
	public void launchPMD() {
		log.info("Launching PMD tool.");
		String[] sourceDirectories = baseConfig.getSourceDirectories();
		String resultsPath = resultsFolder + File.separator
				+ baseConfig.getProjectName();

		File resultDirectory = new File(resultsPath);

		if (!resultDirectory.exists()) {
			resultDirectory.mkdir();
		}

		int holder = baseConfig.getBaseDirectory().lastIndexOf(File.separator);
		String intermediate = baseConfig.getBaseDirectory().substring(
				holder + 1);

		log.info("Checked directory for results from PMD. Now launching task.");

		for (int i = 0; i < sourceDirectories.length; i++) {

			Formatter formatter = new Formatter();
			formatter.setType("xml");
			formatter.setToFile(new File(resultDirectory + File.separator
					+ "PMD--" + intermediate + "--" + i + ".xml"));

			FileSet fs = new FileSet();

			fs.setDir(new File(sourceDirectories[i]));
			fs.setIncludes("*.java");

			RuleSetWrapper rsw = new RuleSetWrapper();

			rsw.addText("Tiger RuleSet");

			RuleSet rs = new RuleSet();

			try {

				rs.getName();

				PMDTask pmdTask = new PMDTask();

				Project p = createProject();

				pmdTask.setProject(p);
				pmdTask.addFormatter(formatter);
				pmdTask.addFileset(fs);
				pmdTask.setTargetJDK(baseConfig.getJdkVersion());

				pmdTask.setRuleSetFiles(SierraTool.getPMDRulesAll().toString());

				pmdTask.execute();
				log.info("PMD tool has finished, now parsing "
						+ resultDirectory + File.separator + "PMD--"
						+ intermediate + "--" + i + ".xml");
				MessageArtifactGenerator generator = new MessageArtifactGenerator();
				Parser parser = new Parser(generator);
				parser.parsePMD(resultDirectory + File.separator + "PMD--"
						+ intermediate + "--" + i + ".xml");
				log.info("PMD file parsed.");
			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to run PMD" + e);
			}

		}

		log.info("Completed PMD execution");

	}

	public void launchJDepend(ToolConfig jDependToolConfig) {

		// JDepend jDepend = new JDepend();
		// try {
		// jDepend.addDirectory("C:\\work\\TigerTesting\\TestFindBugs");
		// jDepend.analyze();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	/**
	 * Helper method to get the current plugin directory, change the relativeURL
	 * to <your-plugin-activator>.getDefault().getBundle().getEntry("");
	 * 
	 * @return
	 */
	public abstract String getToolsDirectory();

}
