package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import org.eclipse.core.runtime.FileLocator;

import com.surelogic.sierra.SierraToolActivator;
import com.surelogic.sierra.SierraToolLogger;
import com.surelogic.sierra.message.MessageArtifactGenerator;
import com.surelogic.sierra.tool.config.ToolConfig;

import edu.umd.cs.findbugs.ExitCodes;

/**
 * The launcher for the tools using ANT api
 * 
 * @author Tanmay.Sinha
 * 
 */
public class Launcher {

	private Java findbugsTask;

	private String findBugsHome;

	private static final Logger log = SierraToolLogger.getLogger("Sierra");

	private static final String TIGER_RESULTS = ".TigerResult";

	private static final Long TIMEOUT = 600000L;

	private static final String FINDBUGS_JAR = "findbugs.jar";

	public Launcher(String name) {

		log.info("\nSTARTING LAUNCHER...\n");
		File resultRoot = new File(TIGER_RESULTS);

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
	public void launchFB(ToolConfig findBugsToolConfig) {
		log.info("Launching FindBugs tool.");

		String baseDirectory = findBugsToolConfig.getBaseDirectory();
		String resultsPath = TIGER_RESULTS + File.separator
				+ findBugsToolConfig.getProjectName();
		String pluginDirectory = getPluginDirectory();

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

			File findBugsJar = new File(findBugsHome + File.separator + "lib"
					+ File.separator + FINDBUGS_JAR);

			findbugsTask.setJar(findBugsJar);

			addArg("-home");
			addArg(findBugsHome);
			addArg("-outputFile");
			addArg(resultDirectory + File.separator + "FB--"
					+ findBugsToolConfig.getProjectName() + ".xml");
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
					+ findBugsToolConfig.getProjectName() + ".xml",
					findBugsToolConfig.getSourceDirectories());
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
	public void launchPMD(ToolConfig pmdToolConfig) {
		log.info("Launching PMD tool.");
		String[] sourceDirectories = pmdToolConfig.getSourceDirectories();
		String resultsPath = TIGER_RESULTS + File.separator
				+ pmdToolConfig.getProjectName();

		File resultDirectory = new File(resultsPath);

		if (!resultDirectory.exists()) {
			resultDirectory.mkdir();
		}

		int holder = pmdToolConfig.getBaseDirectory().lastIndexOf(
				File.separator);
		String intermediate = pmdToolConfig.getBaseDirectory().substring(
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
				pmdTask.setTargetJDK(pmdToolConfig.getJdkVersion());

				pmdTask.setRuleSetFiles(SierraToolActivator.getPMDRulesAll()
						.toString());

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
	private String getPluginDirectory() {

		String commonDirectory = "";

		URL relativeURL = SierraToolActivator.getDefault().getBundle()
				.getEntry("");

		try {

			URL commonPathURL = FileLocator.resolve(relativeURL);
			commonDirectory = commonPathURL.getPath();
			commonDirectory = commonDirectory.replace("/", File.separator);
			// commonDirectory = commonDirectory.substring(1);

			return commonDirectory;

		} catch (IOException e) {
			log.log(Level.SEVERE, "Error getting plugin directory.", e);
		}

		return commonDirectory;

	}

}
