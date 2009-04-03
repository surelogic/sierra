package com.surelogic.sierra.tool;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public abstract class AbstractToolFactory implements IToolFactory {
	private File pluginDir;
	private String version;
	
	public boolean isProduction() {
		return true;
	}
	
	public void init(File toolHome, File pluginDir) {
		this.pluginDir = pluginDir;
		try {
			version = ToolUtil.getPluginVersion(pluginDir);
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't get plugin version", e);
			version = "?.?.?";
		}
	}
	
	public File getPluginDir() {
		return pluginDir;
	}
	
	public String getVersion() {
		return version;
	}
	
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractToolFactory) {
			return this.getClass().equals(o.getClass());
		}
		return false;
	}
}
