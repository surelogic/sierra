package com.surelogic.sierra.tool;

import java.io.File;

public abstract class AbstractToolFactory implements IToolFactory {
	File pluginDir;
	
	public boolean isProduction() {
		return true;
	}
	
	public void init(File toolHome, File pluginDir) {
		this.pluginDir = pluginDir;
	}
	
	@Override
	public final int hashCode() {
		return this.getClass().hashCode();
	}
	
	@Override
	public final boolean equals(Object o) {
		if (o instanceof AbstractToolFactory) {
			return this.getClass().equals(o.getClass());
		}
		return false;
	}
}
