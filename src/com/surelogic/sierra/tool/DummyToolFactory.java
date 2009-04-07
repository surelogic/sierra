package com.surelogic.sierra.tool;

import java.io.File;

import com.surelogic.sierra.tool.message.Config;

public final class DummyToolFactory implements IToolFactory {
	private final String description;
	private final String id;
	private final String name;
	private final String version;
	
	public DummyToolFactory(String id, String version, String name, String desc) {
		this.name = name;
		this.version = version;
		this.id = id;
		this.description = desc;
	}
	
	public ITool create(Config config) {
		throw new UnsupportedOperationException();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
	
	public String getHTMLInfo() {
		return description;
	}

	public File getPluginDir() {
		return null;
	}

	public void init(File toolHome, File pluginDir) {
		// Nothing to do
	}

	public boolean isProduction() {
		return true;
	}
}
