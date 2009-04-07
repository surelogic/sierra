package com.surelogic.sierra.tool;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.surelogic.common.logging.SLLogger;

public abstract class AbstractToolFactory implements IToolFactory {
	private File pluginDir;
	private ToolInfo info;
	
	public boolean isProduction() {
		return true;
	}
	
	public void init(File toolHome, File pluginDir) {
		this.pluginDir = pluginDir;
		try {
			info = ToolUtil.getToolInfo(pluginDir, this.getClass().getName());
			if (info == null) {
				info = new ToolInfo();
			}
		} catch (IOException e) {
			SLLogger.getLogger().log(Level.SEVERE, "Couldn't get tool metadata", e);
		}
	}
	
	public final File getPluginDir() {
		return pluginDir;
	}
	
	public String getId() {
		return info.id;
	}
	
	public String getVersion() {
		return info.version;
	}
	
	public String getName() {
		return info.name;
	}
	
	public String getHTMLInfo() {
		return info.description;
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
