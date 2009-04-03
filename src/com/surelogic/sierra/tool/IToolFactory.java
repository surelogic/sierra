package com.surelogic.sierra.tool;

import java.io.File;

import com.surelogic.sierra.tool.message.Config;

public interface IToolFactory {
	String getId();
	String getName();
	String getVersion();
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
	ITool create(Config config);
}
