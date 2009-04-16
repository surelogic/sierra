package com.surelogic.sierra.tool;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;

final class DummyToolFactory implements IToolFactory {
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
	
	public IToolInstance create(Config config) {
		throw new UnsupportedOperationException();
	}
	
	public IToolInstance create(Config config, ILazyArtifactGenerator gen) {
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

	public Set<ArtifactType> getArtifactTypes() {
		return Collections.emptySet();
	}

	public List<File> getRequiredJars(Config config) {
		return Collections.emptyList();
	}
}
