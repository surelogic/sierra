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
	boolean isProduction();

	/**
	 * If overridden, should call super.init()
	 * 
	 * @param toolHome The general directory for Sierra tool-related stuff
	 * @param pluginDir The specific directory for this tool
	 */
	void init(File toolHome, File pluginDir);
	File getPluginDir();

	/**
	 * Returns all possible artifact types that can be gen'd by this tool 
	 * for db bootstrapping
	 */
	Set<ArtifactType> getArtifactTypes();

	/**
	 * Returns a list of jars required by the tool(s)
	 */
	List<File> getRequiredJars(Config config);

	/**
	 * Creates an instance of the tool to do one scan
	 */
	IToolInstance create(Config config);
	IToolInstance create(Config config, ILazyArtifactGenerator gen);
}
