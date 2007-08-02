/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import org.apache.tools.ant.Project;
import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

/**
 * @author ethan
 * 
 */
public class FindBugsConfig extends ToolConfig {
	private final static String FINDBUGS_CLASS = "edu.umd.cs.findbugs.FindBugs2";
	private static final String MAX_MEMORY = "1024m";

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
	@Override
	public void runTool() {
		// run FindBugs
		CommandlineJava cmdj = new CommandlineJava();
		cmdj.setClassname(FINDBUGS_CLASS);
		cmdj.setMaxmemory(MAX_MEMORY);
		cmdj.createClasspath(antProject).createPath().append(analysis.getClasspath());

		cmdj.createArgument().setValue("-xml");
		cmdj.createArgument().setValue("-outputFile");
		output = new File(analysis.getTmpFolder(), "findbugs.xml");
		cmdj.createArgument().setPath(
				new Path(antProject, output.getAbsolutePath()));
		cmdj.createArgument().setValue("-home");
		// TODO automatically find the FB home
		cmdj.createArgument().setPath(
				new Path(antProject,
						"/Users/ethan/sierra-workspace/sierra-tool/Tools/FB"));
		String[] paths = analysis.getBindir().list();
		for (String string : paths) {
			cmdj.createArgument().setValue(string);
		}

		antProject.log("Executing FindBugs with the commandline: " + cmdj.toString(),
				org.apache.tools.ant.Project.MSG_DEBUG);
		try {
			fork(cmdj.getCommandline());
		} catch (BuildException e) {
			antProject.log("Failed to start FindBugs process.", e,
					org.apache.tools.ant.Project.MSG_ERR);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.surelogic.sierra.tool.ant.ToolConfig#validate()
	 */
	@Override
	public void validate() {
		//do nothing
	}

}
