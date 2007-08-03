package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.CommandlineJava;

import com.surelogic.sierra.tool.analyzer.Parser;

/**
 * Represents a configuration attribute for the PMD tool
 * 
 * @author ethan
 * 
 */
public class PmdConfig extends ToolConfig {
	private final static String PMD_JAR = "pmd-3.9.jar";
	private final static String PMD_CLASS = "net.sourceforge.pmd.PMD";
	private final static String DEFAULT_PMD_JAVA_VERSION = "1.5";

	//TODO add optional rules file
	private String javaVersion = null;

	public PmdConfig(org.apache.tools.ant.Project project) {
		super("pmd", project);
	}
	

	/**
	 * @see {@link ToolConfig#validate()}
	 */
	public void validate() {
		if(name == null || analysis == null){
			throw new BuildException(
					"ToolConfig.initialize() must be called before executing this task.");
		}
		if (javaVersion != null && !javaVersion.matches("\\d\\.\\d")) {
			throw new BuildException(
					"Invalid version string for pmdconfig's 'javaVersion' attribute. Must be one of the following: 1.3, 1.4, 1.5, 1.6 ");
		}
	}

	public void setJavaVersion(String version) {
		this.javaVersion = version;
	}

	public String getJavaVersion() {
		return javaVersion;
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
		cmdj
				.createArgument()
				.setValue(
						"/Users/ethan/sierra-workspace/sierra-tool/Tools/pmd-3.9/all.xml");

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
		assert(analysis != null);
		
		if(!analysis.isJarInClasspath(PMD_JAR)){
			throw new BuildException("PMD is missing dependency: " + PMD_JAR);
		}
		
	}
}
