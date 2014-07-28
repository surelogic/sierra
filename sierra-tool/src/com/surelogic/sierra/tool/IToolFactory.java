package com.surelogic.sierra.tool;

import java.io.File;
import java.util.*;

import org.eclipse.jdt.core.IJavaProject;

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
     * Returns all available extensions to this tool
	 */
	Collection<IToolExtension> getExtensions();

	/**
 	 * If overridden, should merge its results with super.getRequiredJars()
	 * 
	 * @return a list of jars/libraries required by the tool(s)
	 */
	Collection<File> getRequiredJars(Config config);

	/**
	 * Creates an instance of the tool to do one scan
	 * Also creates an ILazyArtifactGenerator appropriate to the Config settings for the results
	 */
	IToolInstance create(Config config);
	
	/**
	 * Creates an instance of the tool to do one scan
	 */
	IToolInstance create(Config config, ILazyArtifactGenerator gen);
	
	/**
	 * @return the explanation as to why the tool cannot be run on this particular project
	 */
	String isRunnableOn(IJavaProject p);
	
	/**
	 * Deactivate this factory due to some error in initialization
	 */
	void deactivate(Throwable e);
}
