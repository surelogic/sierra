/**
 *  Represents the
 *  <tools />
 *  Sub-element in the Ant build file.
 *  
 *  To add support for a new Tool, you need to:
 *  <nl>
 *  <li> Create a sub-class of ToolConfig </li>
 *  <li> Add a default object of that class to this class' tools Map via the {@link #addAllToolDefaults()}</li>
 *  <li> Add an add() method for the class at the bottom of this. Use the existing ones for inspiration </li>
 *  </nl>
 */
package com.surelogic.sierra.tool.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;

import com.surelogic.sierra.tool.analyzer.Parser;
import com.surelogic.sierra.tool.config.Config;

/**
 * A collection for information pertaining to the tools (i.e., FindBugs, PMD)
 * 
 * @author ethan
 * 
 */
public class Tools {
	private final static String FINDBUGS = "findbugs";
	private final static String PMD = "pmd";
	public final static String[] toolList = new String[] { FINDBUGS, PMD };
	
	private org.apache.tools.ant.Project antProject = null;
	private List<String> exclude = new ArrayList<String>();
	private Map<String, ToolConfig> tools = new HashMap<String, ToolConfig>();

	private SierraAnalysis analysis = null;
	private File toolsFolder = null;
	
	static {
		Arrays.sort(toolList);
	}
	
	
	/**
	 * Constructor used by Ant when creating one of these
	 * @param project
	 */
	public Tools(org.apache.tools.ant.Project project){
		this.antProject = project;
		addAllToolDefaults();
	}
	
	/**
	 * Constructor used by Sierra when invoking this Ant Task programmatically
	 * @param project
	 * @param config
	 */
	public Tools(org.apache.tools.ant.Project project, Config config){
		this.antProject = project;
		addAllToolDefaults();
		
		setExclude(config.getExcludedToolsList());
		
		ToolConfig tool;
		Set<String> toolNames = tools.keySet();
		toolsFolder = new File(config.getToolsDirectory());
		for (String toolName : toolNames) {
			tools.get(toolName).configure(config);
		}
		
	}
	
	/**
	 * Adds the default ToolConfig objects to ensure that you don't have to have a sub-element in your build file.
	 * 
	 * XXX Add a default object for ALL tools
	 */
	private void addAllToolDefaults() {
		PmdConfig pmd = new PmdConfig(antProject);
		tools.put(pmd.getToolName(), pmd);
		antProject.log("Added " + pmd.getToolName(), org.apache.tools.ant.Project.MSG_INFO);
		
		FindBugsConfig findbugs = new FindBugsConfig(antProject);
		tools.put(findbugs.getToolName(), findbugs);
		antProject.log("Added " + findbugs.getToolName(), org.apache.tools.ant.Project.MSG_INFO);
	}


	/**
	 * Must be called before execute()
	 * @param analysis
	 */
	void initialize(final SierraAnalysis analysis){
		this.analysis = analysis;
	}

	void validate() {
		if(analysis == null){
			throw new BuildException("Error: initialize() must be called before execute(). Error in Ant Task implementation.");
		}
		
		// TODO should validate the tool names against valid tools
		if (exclude != null) {
			for (String tool : exclude) {
				if (Arrays.binarySearch(toolList, tool) < 0) {
					StringBuffer buf = new StringBuffer();
					buf.append(tool);
					buf
							.append(" is not a valid tool name. Valid tool names are: \n");
					for (String toolName : toolList) {
						buf.append(toolName);
						buf.append("\n");
					}
					throw new BuildException(buf.toString());
				}
			}
		}
		
		if(toolsFolder != null && !toolsFolder.isDirectory()){
			throw new BuildException("toolsFolder must be an existing directory.");
		}
		//TODO check to make sure it *is* our Tools folder
		
		ToolConfig tool;
		for (String toolName : tools.keySet()) {
			tool = tools.get(toolName);
    		tool.initialize(analysis);
    		tool.validate();
		}
	}

	/**
	 * Setter for excludes list
	 * @param list
	 */
	public void setExclude(String list) {
		String[] excludeA = list.split(",");
		String tmp;
		for (int i = 0; i < excludeA.length; i++) {
			tmp = excludeA[i].trim().toLowerCase();
			exclude.add(tmp);
			if(tools.remove(tmp) == null){
            	// This assumes that someone will not add the same tool name twice
				antProject.log("Warning: " + tmp + " is not a valid tool name.", org.apache.tools.ant.Project.MSG_WARN);
			}
		}
	}

	/**
	 * Getter for the excludes list
	 * @return
	 */
	public List<String> getExclude() {
		return exclude;
	}
	
	/**
	 * Verifies that all tool dependencies exist in the <taskdef>'s classpath
	 */
	void verifyToolDependencies() {
		ToolConfig tool;
		Set<String> toolNames = tools.keySet();
		for (String toolName : toolNames) {
			tool = tools.get(toolName);
			tool.verifyDependencies();
		}
	}

	
	/**
	 * Runs all of the included tools
	 */
	void runTools(){
		antProject.log("Running tools...", org.apache.tools.ant.Project.MSG_INFO);
		antProject.log("Source path: " + analysis.getSrcdir(), org.apache.tools.ant.Project.MSG_DEBUG);
		antProject.log("Binary path: " + analysis.getBindir(), org.apache.tools.ant.Project.MSG_DEBUG);
		antProject.log("Results will be saved to: " + analysis.getTmpFolder(),
				org.apache.tools.ant.Project.MSG_DEBUG);
		
		ToolConfig tool;
		Set<String> toolNames = tools.keySet();
		for (String toolName : toolNames) {
			antProject.log("Running tool: " + toolName, org.apache.tools.ant.Project.MSG_DEBUG);
			tool = tools.get(toolName);
			tool.runTool();
		}
	}
	
	/**
	 * Tells each tool to parse their own output
	 * @param parser
	 */
	void parseOutput(Parser parser) {
		ToolConfig tool;
		Set<String> toolNames = tools.keySet();
		for (String toolName : toolNames) {
			antProject.log("Parsing " + toolName + " output.", 
				org.apache.tools.ant.Project.MSG_DEBUG);
			tool = tools.get(toolName);
			tool.parseOutput(parser);
		}
	}
	
	/**
	 * Makes all of the tools clean up their files
	 */
	public void cleanup() {
		ToolConfig tool;
		Set<String> toolNames = tools.keySet();
		for (String toolName : toolNames) {
			tool = tools.get(toolName);
			tool.cleanup();
		}
	}
	
	/**
	 * Getter for the Tools folder
	 * @return
	 */
	public File getToolsFolder() {
		if(toolsFolder == null || !toolsFolder.isDirectory()){
			String[] paths = analysis.getClasspath().list();
			for (String path : paths) {
				if(path.endsWith("findbugs.jar")){
					int index = path.indexOf("FB");
					toolsFolder = new File(path.substring(0, index - 1));
				}
			}
		}
		return toolsFolder;
	}

	/**
	 * Setter for the Tools folder
	 * @param toolsFolder
	 */
	public void setToolsFolder(File toolsFolder) {
		this.toolsFolder = toolsFolder;
	}

	
	
	/* **********************************************************
	 * 
	 * Specific ToolConfig methods
	 * 
	 * XXX Add methods for all new tools here of the form: add<ToolConfigClassName>(<ToolConfigClassName> config)
	 * 
	 ************************************************************/
	
	public void addPmdConfig(PmdConfig config) {
		tools.remove(config.getToolName());
		tools.put(config.getToolName(), config);
	}
	
	public void addFindBugsConfig(FindBugsConfig config){
		tools.remove(config.getToolName());
		tools.put(config.getToolName(), config);
	}

}
