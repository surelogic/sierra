package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.IOException;

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
	
	//The path to the default rules file, relative to the Tools folder
	private static final String RULES_FILE_PATH = "pmd-3.9" + File.separator + "all.xml";

	private String javaVersion = null;
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

		if (javaVersion != null && !javaVersion.matches("\\d\\.\\d")) {
			throw new BuildException(
					"Invalid version string for pmdconfig's 'javaVersion' attribute. Must be one of the following: 1.3, 1.4, 1.5, 1.6 ");
		}
		if (rulesFile != null && !rulesFile.isFile()) {
			throw new BuildException(
					"rulesfile must be a valid PMD rules XML file.");
			//TODO can we check and make sure it is a valid PMD xml file?
		} else {
			rulesFile = new File(analysis.getSierraTools().getToolsFolder(), RULES_FILE_PATH);
		}
	}

	/**
	 * @see {@link ToolConfig#runTool()}
	 */
	public void runTool() {
		// run PMD
		CommandlineJava cmdj = new CommandlineJava();

		// Add the class to run
		cmdj.setClassname(PMD_CLASS);

		// Set the Java command's classpath
		cmdj.createClasspath(antProject).createPath().append(
				analysis.getClasspath());

		// Add the output file
		cmdj.createArgument().setValue("-reportfile");

		output = new File(analysis.getTmpFolder(), "pmd.xml");
		try {
			output.createNewFile();
		} catch (IOException e1) {
			antProject.log("Error creating PMD output file: "
					+ output.getAbsolutePath(),
					org.apache.tools.ant.Project.MSG_ERR);
		}
		cmdj.createArgument().setValue(output.getAbsolutePath());

		antProject.log("Classpath: " + cmdj.getClasspath().toString(),
				org.apache.tools.ant.Project.MSG_DEBUG);

		// Add the source directories to scan
		String[] paths = analysis.getSrcdir().list();
		String csv = analysis.arrayToCSV(paths);
		antProject.log("Source path: " + csv,
				org.apache.tools.ant.Project.MSG_DEBUG);

		cmdj.createArgument().setValue(csv);

		// Add the output format
		cmdj.createArgument().setValue("xml");

		// Add the ruleset file
		cmdj.createArgument().setValue(rulesFile.getAbsolutePath());

		// Add optional arguments
		if (javaVersion != null && !"".equals(javaVersion)) {
			cmdj.createArgument().setValue("-targetjdk");
			cmdj.createArgument().setValue(getJavaVersion());
		}

		antProject.log(
				"Executing PMD with the commandline: " + cmdj.toString(),
				org.apache.tools.ant.Project.MSG_DEBUG);
		try {

			fork(cmdj.getCommandline());
		} catch (BuildException e) {
			antProject.log("Failed to start PMD process.", e,
					org.apache.tools.ant.Project.MSG_ERR);
		}

	}

	@Override
	public void parseOutput(Parser parser) {
		if (output != null && output.exists()) {
			antProject.log("Parsing PMD results file: " + output,
					org.apache.tools.ant.Project.MSG_INFO);
			parser.parsePMD(output.getAbsolutePath());
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
		setJavaVersion(config.getJavaVersion());
		setRulesFile(config.getPmdRulesFile());
	}
	
	@Override 
	void cleanup(){
		output.delete();
	}

	/**
	 * @return the rulesFile
	 */
	final File getRulesFile() {
		return rulesFile;
	}

	/**
	 * @param rulesFile
	 *            the rulesFile to set
	 */
	final void setRulesFile(File rulesFile) {
		this.rulesFile = rulesFile;
	}

	public void setJavaVersion(String version) {
		this.javaVersion = version;
	}

	public String getJavaVersion() {
		return javaVersion;
	}
}
