package com.surelogic.sierra.tool;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

public interface IToolFactory {
	/**
	 * e.g. "FindBugs"
	 */
	String getId();
	
	/**
	 * e.g. "FindBugs (TM)"
	 */
	String getName();

	/**
	 * e.g. "1.3.0"
	 */
	String getVersion();
	
	/**
	 * e.g. "<a href="http://findbugs.sf.net">FindBugs</a> is a blahblahblah"
	 */
	String getHTMLInfo();
	
	/**
	 * @return true if it is production-ready (e.g. not experimental)
	 */
	boolean isProduction();

	/**
	 * Initializes the factory
	 * If overridden, should call super.init()
	 * 
	 * @param toolHome The general directory for Sierra tool-related stuff
	 * @param pluginDir The specific directory for this tool
	 */
	void init(File toolHome, File pluginDir);
	
	/**
	 * @return the location of the originating tool directory
	 */
	File getPluginDir();

	/**
	 * Returns all possible artifact types that can be gen'd by this tool 
	 * for db bootstrapping
	 */
	Set<ArtifactType> getArtifactTypes();

	/**
 	 * If overridden, should merge its results with super.getRequiredJars()
	 * 
	 * @return a list of jars/libraries required by the tool(s)
	 */
	List<File> getRequiredJars(Config config);

	/**
	 * Creates an instance of the tool to do one scan
	 * Also creates an ILazyArtifactGenerator appropriate to the Config settings for the results
	 */
	IToolInstance create(Config config);
	
	/**
	 * Creates an instance of the tool to do one scan
	 */
	IToolInstance create(Config config, ILazyArtifactGenerator gen);
}
