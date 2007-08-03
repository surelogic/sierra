package com.surelogic.sierra.tool.analyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
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

import com.surelogic.sierra.tool.SierraLogger;
import com.surelogic.sierra.tool.SierraTool;
import com.surelogic.sierra.tool.config.BaseConfig;
import com.surelogic.sierra.tool.config.Config;
import com.surelogic.sierra.tool.config.ToolConfig;
import com.surelogic.sierra.tool.message.MessageArtifactFileGenerator;

import edu.umd.cs.findbugs.ExitCodes;

/**
 * The launcher for the tools using ANT api
 * 
 * @author Tanmay.Sinha
 * 
 */
public abstract class Launcher {

	private Java findbugsTask;

	private BaseConfig baseConfig;

	private String tmpDir;

	private String resultsFolder;

	private File resultDirectory;

	private final String[] sourceDirectories;

	private String intermediate;

	private Java pmdTask;

	private static final Logger log = SierraLogger.getLogger("Sierra");

	private static final String TIGER_RESULTS = ".TigerResult";

	private static final Long TIMEOUT = 600000L;

	private static final String FINDBUGS_JAR = "findbugs.jar";

	private static final String PARSED_FILE_SUFFIX = ".parsed";

	private static final String PMD_JAR = "pmd-3.9.jar";

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

		// This code computes the source directories from the given base
		// directory
		File root = new File(baseConfig.getBaseDirectory());
		JavaFilter filter = new JavaFilter();
		filterdirs(root, filter);

		Iterator<File> dirIterator = filter.dirs.iterator();
		Vector<String> sourceDirectory = new Vector<String>();
		while (dirIterator.hasNext()) {
			File holder = dirIterator.next();
			sourceDirectory.add(holder.getPath());
		}

		sourceDirectories = sourceDirectory.toArray(new String[sourceDirectory
				.size()]);
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
	private void addArg(Java task, String arg) {
		task.createArg().setValue(arg);
	}

	/**
	 * Launch FindBugs
	 * 
	 * @param findBugsToolConfig
	 */
	public void launchFB() {
		log.info("Launching FindBugs...");

		String baseDirectory = baseConfig.getBaseDirectory();
		String resultsPath = resultsFolder + File.separator
				+ baseConfig.getProjectName();
		String pluginDirectory = baseConfig.getToolsDirectory();

		resultDirectory = new File(resultsPath);

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

			String findBugsHome = pluginDirectory + "Tools" + File.separator
					+ "FB";

			// log.info("______" + findBugsHome);

			File findBugsJar = new File(findBugsHome + File.separator + "lib"
					+ File.separator + FINDBUGS_JAR);

			findbugsTask.setJar(findBugsJar);

			addArg(findbugsTask, "-home");
			addArg(findbugsTask, findBugsHome);
			addArg(findbugsTask, "-outputFile");
			addArg(findbugsTask, resultDirectory + File.separator + "FB--"
					+ baseConfig.getProjectName() + ".xml");
			addArg(findbugsTask, "-xml:withMessages");
			addArg(findbugsTask, baseDirectory);

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
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to run findbugs" + e);
		}
	}

	// public void parseFB() {
	// MessageArtifactGenerator generator = new MessageArtifactGenerator();
	// Parser parser = new Parser(generator);
	// String toolFile = resultDirectory + File.separator + "FB--"
	// + baseConfig.getProjectName() + ".xml";
	// parser.parseFB(toolFile, sourceDirectories);
	// generator.write(toolFile + PARSED_FILE_SUFFIX);
	// log.info("Findbugs file parsed.");
	//
	// log.info("Completed Findbugs execution.");
	// }

	public void launchPMDANT() {
		log.info("Launching PMD ANT...");

		String baseDirectory = baseConfig.getBaseDirectory();
		String resultsPath = resultsFolder + File.separator
				+ baseConfig.getProjectName();

		String pluginDirectory = baseConfig.getToolsDirectory();
		resultDirectory = new File(resultsPath);

		if (!resultDirectory.exists()) {
			resultDirectory.mkdir();
		}

		log
				.info("Checked directory for results from PMD ANT. Now launching task.");

		try {
			Project p = createProject();
			pmdTask = (Java) p.createTask("java");

			pmdTask.setTaskName("PMDANT");
			pmdTask.setFork(true);
			pmdTask.createJvmarg().setLine("-Xmx1024M");
			pmdTask.setTimeout(TIMEOUT);

			String pmdDir = pluginDirectory + "Tools" + File.separator
					+ "pmd-3.9";
			File pmdJar = new File(pmdDir + File.separator + "lib"
					+ File.separator + PMD_JAR);
			File rulesXML = new File(pmdDir + File.separator + "all.xml");

			pmdTask.setJar(pmdJar);

			addArg(pmdTask, baseDirectory);
			addArg(pmdTask, "xml");
			addArg(pmdTask, rulesXML.getAbsolutePath());
			addArg(pmdTask, "-reportfile");
			addArg(pmdTask, resultDirectory + File.separator + "PMD--"
					+ baseConfig.getProjectName() + ".xml");

			pmdTask.executeJava();

			log.info("PMD ANT has finished, now parsing file.");
		} catch (BuildException e) {
			log.log(Level.SEVERE, "Unable to run PMD ANT" + e);
		}
	}

	/**
	 * Launch PMD
	 * 
	 * @param pmdToolConfig
	 */
	public void launchPMD() {
		log.info("Launching PMD...");
		String resultsPath = resultsFolder + File.separator
				+ baseConfig.getProjectName();

		File resultDirectory = new File(resultsPath);

		if (!resultDirectory.exists()) {
			resultDirectory.mkdir();
		}

		int holder = baseConfig.getBaseDirectory().lastIndexOf(File.separator);
		intermediate = baseConfig.getBaseDirectory().substring(holder + 1);

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
				log.info("PMD has generated \"" + resultDirectory
						+ File.separator + "PMD--" + intermediate + "--" + i
						+ ".xml\"");

			} catch (Exception e) {
				log.log(Level.SEVERE, "Unable to run PMD" + e);
			}

		}

		log.info("Completed PMD execution");

	}

	// public void parsePMD() {
	// for (int i = 0; i < sourceDirectories.length; i++) {
	// log.info("Parsing file " + resultDirectory + File.separator
	// + "PMD--" + intermediate + "--" + i + ".xml");
	// MessageArtifactGenerator generator = new MessageArtifactGenerator();
	// Parser parser = new Parser(generator);
	// String toolFile = resultDirectory + File.separator + "PMD--"
	// + intermediate + "--" + i + ".xml";
	// parser.parsePMD(toolFile);
	// generator.write(toolFile + PARSED_FILE_SUFFIX);
	// log.info("PMD file parsed.");
	// }
	// }

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
	 * Helper method to get the current plugin directory, change the relativeURL
	 * to <your-plugin-activator>.getDefault().getBundle().getEntry("");
	 * 
	 * @return
	 */
	public abstract String getToolsDirectory();

	public void parseFiles() {

		Config config = new Config(baseConfig);
		String toolFileFB = resultDirectory + File.separator + "FB--"
				+ baseConfig.getProjectName() + ".xml";
		String toolFilePMD = resultDirectory + File.separator + "PMD--"
				+ baseConfig.getProjectName() + ".xml";

		String parsedFileName = resultDirectory + File.separator
				+ baseConfig.getProjectName() + ".xml" + PARSED_FILE_SUFFIX;
		MessageArtifactFileGenerator generator = new MessageArtifactFileGenerator(
				parsedFileName, config);
		Parser parser = new Parser(generator);

		// log.info("Starting parse of results FOR HASH...");
		// Map<String, Map<Integer, Long>> hashHolder = new HashMap<String,
		// Map<Integer, Long>>();
		// parser.parseForHash(toolFileFB, hashHolder, sourceDirectories);
		// parser.parseForHash(toolFilePMD, hashHolder, sourceDirectories);
		// log.info("Finished parse of results FOR HASH.");
		//
		// log.info("Generating hash...");
		// HashGenerator hashGenerator = HashGenerator.getInstance();
		// hashGenerator.generateHash(hashHolder);
		// log.info("Finished hash generation.");

		// Set<String> fileNames = hashHolder.keySet();
		//
		// Iterator<String> files = fileNames.iterator();
		//
		// while (files.hasNext()) {
		// String temp = files.next();
		// System.out.println(temp);
		//
		// Map<Integer, Long> lineHashMap = hashHolder.get(temp);
		// Set<Integer> lineNumbers = lineHashMap.keySet();
		//
		// Iterator<Integer> lineNumberIterator = lineNumbers.iterator();
		//
		// while (lineNumberIterator.hasNext()) {
		// Integer lineNumber = lineNumberIterator.next();
		//
		// Long hash = lineHashMap.get(lineNumber);
		// System.out.println("\tLine number :" + lineNumber + " Hash :"
		// + hash);
		// }
		//
		// }

		log.info("Starting parse of FindBugs results...");
		parser.parseFB(toolFileFB, sourceDirectories);
		log.info("Findbugs file parsed.");

		log.info("Completed Findbugs execution.");

		log.info("Starting parse of PMD results...");
		 parser.parsePMD(toolFilePMD);

		// for (int i = 0; i < sourceDirectories.length; i++) {
		// log.info("Parsing file " + resultDirectory + File.separator
		// + "PMD--" + intermediate + "--" + i + ".xml");
		// toolFilePMD = resultDirectory + File.separator + "PMD--"
		// + intermediate + "--" + i + ".xml";
		// parser.parsePMD(toolFilePMD);
		//
		// }

		log.info("PMD file parsed.");
		log.info("Completed PMD execution.");

		generator.write();
		// parseFB();
		// parsePMD();

	}
}
